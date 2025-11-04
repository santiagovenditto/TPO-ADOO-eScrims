#!/usr/bin/env bash
# Simple start script: free port 8080 if needed and start the Java server
set -eu
ROOT="$(cd "$(dirname "$0")" && pwd)"
LOG="$ROOT/server.log"

echo "Starting server from $ROOT"

# If any process listens on 8080, try to kill it
if lsof -iTCP:8080 -sTCP:LISTEN -n -P >/dev/null 2>&1; then
  PIDS=$(lsof -tiTCP:8080 -sTCP:LISTEN -n -P)
  echo "Port 8080 in use by: $PIDS — attempting to kill"
  kill $PIDS || true
  sleep 1
fi

echo "Launching java -cp out Main — logs -> $LOG"
nohup java -cp "$ROOT/out" Main > "$LOG" 2>&1 &
sleep 0.5
echo "Server started (check $LOG for output)"
