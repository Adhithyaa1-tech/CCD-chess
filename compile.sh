#!/bin/zsh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
BUNDLED_JAVA="$ROOT/.jdk/jdk-22.0.2+9/Contents/Home"

if [[ -x "$BUNDLED_JAVA/bin/java" && -x "$BUNDLED_JAVA/bin/javac" ]]; then
  JAVA_HOME="$BUNDLED_JAVA"
elif [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" && -x "$JAVA_HOME/bin/javac" ]]; then
  JAVA_HOME="$JAVA_HOME"
else
  echo "Required JDK 22 was not found at $BUNDLED_JAVA or in JAVA_HOME" >&2
  exit 1
fi

export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

SRC="$ROOT/joChess-1.0.1-sources"
OUT="$ROOT/out"

CP="$ROOT/lib/appframework-1.0.3.jar:$ROOT/lib/swing-worker-1.1.jar"

mkdir -p "$OUT"
cd "$SRC"
find . -name '*.java' > /tmp/jochess-sources.txt
javac --release 22 -encoding UTF-8 -cp "$CP" -d "$OUT" @/tmp/jochess-sources.txt

rsync -a --exclude '*.java' --exclude '*.form' --exclude '.DS_Store' "$SRC/jchess" "$OUT/"
mkdir -p "$OUT/META-INF/services"
if [[ -f "$SRC/META-INF/services/org.jdesktop.application.Application" ]]; then
  cp "$SRC/META-INF/services/org.jdesktop.application.Application" "$OUT/META-INF/services/"
fi

echo "Compiled with $(java -version 2>&1 | head -1)"
echo "Run with: ./run.sh"
