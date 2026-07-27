# Project Poseidon Monitoring

This is a local setup for testing monitoring of Poseidon server.

Server exposes Prometheus metrics directly from the server process at:

- `http://<server-host>:9464/metrics` (default)

The endpoint is controlled by `server.properties`:

- `prometheus.enabled=true`
- `prometheus.host=0.0.0.0`
- `prometheus.port=9464`

## Quick Start

From repository root:

```bash
mvn clean package
cd monitoring
docker compose up -d
docker compose exec poseidon sh
```

Container data files are stored at:

- `monitoring/poseidon-data`

## Run Poseidon In Java 8 Container

Build the server jar first from repository root:

```bash
mvn clean package
```

Then start Poseidon from `monitoring/`:

```bash
docker compose up -d poseidon
docker compose logs -f poseidon
```

Recommended mode is detached (`-d`) so Prometheus/Grafana and the server keep running in the background.

## Open A Shell In The Poseidon Container

From `monitoring/`:

```bash
docker compose exec poseidon sh
```

Or by container name:

```bash
docker exec -it poseidon-server sh
```

Container details:

- Java runtime: `eclipse-temurin:8-jre`
- JAR mount: `../target -> /opt/poseidon/target` (read-only)
- Server data directory: `monitoring/poseidon-data`
- Exposed ports: `25565` (MC), `9464` (Prometheus)
- If `server.properties` is missing in `poseidon-data/`, the startup script creates one with Prometheus enabled on `0.0.0.0:9464`

Optional overrides:

- Force specific JAR from `target/`:
  `POSEIDON_JAR=<JAR_NAME>.jar docker compose up -d poseidon`
- Set JVM heap:
  `JAVA_XMS=1g JAVA_XMX=2g docker compose up -d poseidon`
- Add extra JVM options:
  `JAVA_OPTS="-XX:+UseG1GC" docker compose up -d poseidon`

## Local Monitoring Stack (Prometheus + Grafana)

From `monitoring/`:

```bash
docker compose up -d prometheus grafana
```

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (Credentials: `admin` / `admin`)

Open Included dashboard `Project Poseidon - Server Overview`.
Left sidebar `Dashboards` then navigation the dashboard tree or directly with url `http://localhost:3000/d/poseidon-overview/project-poseidon-server-overview` 

## Prometheus

Prometheus is configured to try scrape both locally (docker network specific):

1. `poseidon:9464` (docker compose network)
2. Host fallbacks: `host.docker.internal:9464`, `172.17.0.1:9464`

You can check Prometheus -> Poseidon connection by accessing the targets. Either by (top menu) `Status > Targets` or 
directly to `http://localhost:9090/targets`. In Prometheus landing page you can also run one of the following expressions,
   - `up{job="project-poseidon"}`
   - `poseidon_server_up`