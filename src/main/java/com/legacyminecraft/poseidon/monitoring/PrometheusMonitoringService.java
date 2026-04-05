package com.legacyminecraft.poseidon.monitoring;

import io.prometheus.client.Counter;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerConfigurationManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PrometheusMonitoringService {
    private static final Logger LOGGER = Logger.getLogger("Minecraft");
    private static final int TPS_WINDOW_SECONDS = 60;
    private static final int TICKS_PER_SECOND = 20;
    private static boolean defaultExportsInitialized = false;

    private final MinecraftServer server;
    private final String host;
    private final int port;

    private final Gauge poseidonServerUp = Gauge.build()
            .name("poseidon_server_up")
            .help("Poseidon server process up status (1=up, 0=down).")
            .register();
    private final Gauge poseidonPlayersOnline = Gauge.build()
            .name("poseidon_players_online")
            .help("Current number of online players.")
            .register();
    private final Gauge poseidonPlayersMax = Gauge.build()
            .name("poseidon_players_max")
            .help("Configured maximum players.")
            .register();
    private final Gauge poseidonWorldsLoaded = Gauge.build()
            .name("poseidon_worlds_loaded")
            .help("Current number of loaded worlds.")
            .register();
    private final Gauge poseidonTps = Gauge.build()
            .name("poseidon_tps")
            .help("Average TPS over the latest one-minute window.")
            .register();
    private final Gauge poseidonTickDurationMillisLast = Gauge.build()
            .name("poseidon_tick_duration_millis_last")
            .help("Duration of the most recent server tick in milliseconds.")
            .register();
    private final Gauge poseidonTickDurationMillisAvg1m = Gauge.build()
            .name("poseidon_tick_duration_millis_avg_1m")
            .help("Average tick duration over the latest minute in milliseconds.")
            .register();
    private final Histogram poseidonTickDurationSeconds = Histogram.build()
            .name("poseidon_tick_duration_seconds")
            .help("Tick duration distribution in seconds.")
            .buckets(0.01D, 0.02D, 0.03D, 0.05D, 0.1D, 0.2D, 0.5D, 1.0D)
            .register();
    private final Counter poseidonSlowTicks = Counter.build()
            .name("poseidon_slow_ticks_total")
            .help("Number of ticks that exceeded 50ms.")
            .register();

    // Basic aliases for dashboards built around older Bukkit exporter names.
    private final Gauge mcPlayersOnlineTotal = Gauge.build()
            .name("mc_players_online_total")
            .help("Alias for current online player count.")
            .register();
    private final Gauge mcTps = Gauge.build()
            .name("mc_tps")
            .help("Alias for average TPS over the latest one-minute window.")
            .register();
    private final Gauge mcTickDurationAverage = Gauge.build()
            .name("mc_tick_duration_average")
            .help("Alias for average tick duration over the latest minute in milliseconds.")
            .register();

    private final Deque<Long> recentTickDurationsNanos = new ArrayDeque<Long>();
    private final int recentTickWindowSize = TPS_WINDOW_SECONDS * TICKS_PER_SECOND;
    private long recentTickDurationsTotalNanos = 0L;

    private HTTPServer httpServer;

    private PrometheusMonitoringService(MinecraftServer server, String host, int port) {
        this.server = server;
        this.host = host;
        this.port = port;
    }

    public static PrometheusMonitoringService startIfEnabled(MinecraftServer server) {
        if (server == null || server.propertyManager == null) {
            return null;
        }

        boolean enabled = server.propertyManager.getBoolean("prometheus.enabled", true);
        if (!enabled) {
            LOGGER.info("[Prometheus] Metrics exporter disabled via server.properties (prometheus.enabled=false).");
            return null;
        }

        String host = server.propertyManager.getString("prometheus.host", "0.0.0.0");
        int port = server.propertyManager.getInt("prometheus.port", 9464);
        PrometheusMonitoringService service = new PrometheusMonitoringService(server, host, port);

        try {
            service.start();
            LOGGER.info("[Prometheus] Metrics exporter started on http://" + host + ":" + port + "/metrics");
            return service;
        } catch (Throwable throwable) {
            LOGGER.log(Level.WARNING, "[Prometheus] Failed to start metrics exporter. Server will continue without Prometheus metrics.", throwable);
            service.stop();
            return null;
        }
    }

    private static synchronized void initializeDefaultExportsIfNeeded() {
        if (!defaultExportsInitialized) {
            DefaultExports.initialize();
            defaultExportsInitialized = true;
        }
    }

    private void start() throws IOException {
        initializeDefaultExportsIfNeeded();
        this.httpServer = new HTTPServer(new InetSocketAddress(this.host, this.port), CollectorRegistry.defaultRegistry, true);
        this.poseidonServerUp.set(1D);
        this.poseidonTickDurationMillisLast.set(0D);
        this.poseidonTickDurationMillisAvg1m.set(0D);
        this.poseidonTps.set(20D);
        this.mcTps.set(20D);
    }

    public void stop() {
        this.poseidonServerUp.set(0D);
        if (this.httpServer != null) {
            this.httpServer.stop();
            this.httpServer = null;
        }
    }

    public void onTickComplete(long tickDurationNanos) {
        if (tickDurationNanos < 0L) {
            return;
        }

        double tickDurationMillis = tickDurationNanos / 1_000_000.0D;
        this.poseidonTickDurationMillisLast.set(tickDurationMillis);
        this.poseidonTickDurationSeconds.observe(tickDurationNanos / 1_000_000_000.0D);

        if (tickDurationNanos > 50_000_000L) {
            this.poseidonSlowTicks.inc();
        }

        double averageTickDurationMillis = updateRecentTickAverage(tickDurationNanos);
        this.poseidonTickDurationMillisAvg1m.set(averageTickDurationMillis);
        this.mcTickDurationAverage.set(averageTickDurationMillis);

        int onlinePlayers = 0;
        int maxPlayers = 0;
        ServerConfigurationManager configurationManager = this.server.serverConfigurationManager;
        if (configurationManager != null) {
            onlinePlayers = configurationManager.players.size();
            maxPlayers = configurationManager.maxPlayers;
        }

        this.poseidonPlayersOnline.set(onlinePlayers);
        this.poseidonPlayersMax.set(maxPlayers);
        this.poseidonWorldsLoaded.set(this.server.worlds == null ? 0 : this.server.worlds.size());
        this.mcPlayersOnlineTotal.set(onlinePlayers);

        double currentTps = calculateAverageTps(this.server.getTpsRecords(), TPS_WINDOW_SECONDS);
        this.poseidonTps.set(currentTps);
        this.mcTps.set(currentTps);
    }

    private double updateRecentTickAverage(long tickDurationNanos) {
        this.recentTickDurationsNanos.addLast(tickDurationNanos);
        this.recentTickDurationsTotalNanos += tickDurationNanos;

        while (this.recentTickDurationsNanos.size() > this.recentTickWindowSize) {
            Long removed = this.recentTickDurationsNanos.removeFirst();
            this.recentTickDurationsTotalNanos -= removed.longValue();
        }

        if (this.recentTickDurationsNanos.isEmpty()) {
            return 0D;
        }

        double averageNanos = this.recentTickDurationsTotalNanos / (double) this.recentTickDurationsNanos.size();
        return averageNanos / 1_000_000.0D;
    }

    private double calculateAverageTps(LinkedList<Double> records, int maxSeconds) {
        if (records == null || records.isEmpty()) {
            return 20D;
        }

        int used = 0;
        double sum = 0D;

        synchronized (records) {
            int maxRecords = Math.min(records.size(), maxSeconds);
            for (int i = 0; i < maxRecords; i++) {
                Double value = records.get(i);
                if (value == null) {
                    continue;
                }
                sum += value.doubleValue();
                used++;
            }
        }

        if (used == 0) {
            return 20D;
        }

        return sum / used;
    }
}
