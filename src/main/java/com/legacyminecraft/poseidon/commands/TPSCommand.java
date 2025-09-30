package com.legacyminecraft.poseidon.commands;

import com.legacyminecraft.poseidon.Poseidon;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class TPSCommand extends Command {

    private final LinkedHashMap<String, Integer> intervals = new LinkedHashMap<>();
    private final long startTime;

    public TPSCommand(String name) {
        super(name);
        this.description = "Shows the server's TPS for various intervals";
        this.usageMessage = "/tps";
        this.setPermission("poseidon.command.tps");

        intervals.put("5s", 5);
        intervals.put("30s", 30);
        intervals.put("1m", 60);
        intervals.put("5m", 300);
        intervals.put("10m", 600);
        intervals.put("15m", 900);

        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;

        LinkedList<Double> tpsRecords = Poseidon.getTpsRecords();
        long uptimeSeconds = (System.currentTimeMillis() - startTime) / 1000;

        StringBuilder message = new StringBuilder("§bServer TPS: [");
        boolean first = true;

        for (Map.Entry<String, Integer> entry : intervals.entrySet()) {
            int requiredSeconds = entry.getValue();

            if (uptimeSeconds < requiredSeconds) continue;

            double avg = calculateAverage(tpsRecords, requiredSeconds);
            if (!first) message.append("§7, ");
            message.append(entry.getKey()).append(": ").append(formatTps(avg));
            first = false;
        }

        message.append("§b]");

        if (first) {
            sender.sendMessage("§eServer is still starting up. Not enough uptime to calculate TPS averages yet.");
        } else {
            sender.sendMessage(message.toString());
        }

        return true;
    }

    private double calculateAverage(LinkedList<Double> records, int seconds) {
        int size = Math.min(records.size(), seconds);
        if (size == 0) return 20.0;

        double total = 0;
        for (int i = 0; i < size; i++) {
            total += records.get(i);
        }
        return total / size;
    }

    private String formatTps(double tps) {
        String color = (tps >= 19) ? "§a" : (tps >= 15) ? "§e" : "§c";
        return color + String.format("%.2f", Math.min(tps, 20.0));
    }
}
