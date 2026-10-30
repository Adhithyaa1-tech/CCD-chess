#!/bin/zsh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
BUNDLED_JAVA="$ROOT/.jdk/jdk-22.0.2+9/Contents/Home"
if [[ -x "$BUNDLED_JAVA/bin/java" ]]; then
  JAVA_HOME="$BUNDLED_JAVA"
elif [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
  JAVA_HOME="$JAVA_HOME"
else
  echo "Required JDK 22 was not found at $BUNDLED_JAVA or in JAVA_HOME" >&2
  exit 1
fi
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

CP="$ROOT/out:$ROOT/lib/appframework-1.0.3.jar:$ROOT/lib/swing-worker-1.1.jar"

echo "Using $(java -version 2>&1 | head -1)"
cd "$ROOT"
exec java -cp "$CP" jchess.JChessApp "$@"
