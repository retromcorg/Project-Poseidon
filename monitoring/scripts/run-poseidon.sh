#!/bin/sh
set -eu

TARGET_DIR="/opt/poseidon/target"
DATA_DIR="/opt/poseidon/data"

cd "${DATA_DIR}"

if [ -n "${POSEIDON_JAR:-}" ]; then
  jar_file="${TARGET_DIR}/${POSEIDON_JAR}"
else
  jar_file="$(ls -1 "${TARGET_DIR}"/*.jar 2>/dev/null | grep -Ev '/original-|sources|javadoc' | head -n 1 || true)"
fi

if [ -z "${jar_file}" ] || [ ! -f "${jar_file}" ]; then
  echo "No runnable Poseidon jar found in ${TARGET_DIR}" >&2
  echo "Build it first from repo root: mvn clean package" >&2
  echo "Then start compose from monitoring/: docker compose up -d poseidon" >&2
  exit 1
fi

if [ ! -f "server.properties" ]; then
  cat > server.properties <<'PROPS'
server-ip=0.0.0.0
server-port=25565
prometheus.enabled=true
prometheus.host=0.0.0.0
prometheus.port=9464
PROPS
fi

echo "Starting Poseidon from ${jar_file}"

JAVA_XMS="${JAVA_XMS:-512m}"
JAVA_XMX="${JAVA_XMX:-1024m}"
JAVA_OPTS="${JAVA_OPTS:-}"

# JAVA_OPTS is intentionally unquoted to allow multiple JVM flags.
exec java -Xms"${JAVA_XMS}" -Xmx"${JAVA_XMX}" ${JAVA_OPTS} -jar "${jar_file}" --nogui
