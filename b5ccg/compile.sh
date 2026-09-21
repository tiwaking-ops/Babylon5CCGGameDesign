#!/bin/bash
# Compile the B5 CCG Java project (Java 8, no external libraries)
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/src"
OUT="$ROOT/out"
RES="$ROOT/resources"

echo "=== B5 CCG Build ==="
echo "Source: $SRC"
echo "Output: $OUT"

mkdir -p "$OUT"

# Collect all .java files
JAVA_FILES=$(find "$SRC" -name "*.java" | tr '\n' ' ')

if [ -z "$JAVA_FILES" ]; then
  echo "ERROR: No .java files found in $SRC"
  exit 1
fi

echo "Compiling $(echo "$JAVA_FILES" | wc -w | tr -d ' ') source files…"
javac -source 8 -target 8 -encoding UTF-8 -d "$OUT" $JAVA_FILES

echo "Copying resources…"
if [ -d "$RES" ]; then
  cp -r "$RES"/. "$OUT"/
fi

echo ""
echo "✓ Build successful. Run with: ./run.sh"
