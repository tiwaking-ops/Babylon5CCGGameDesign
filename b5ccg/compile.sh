#!/bin/bash
# Compile the B5 CCG Java project (Java 6 only, no external libraries).
set -e

# Resolve the directory this script lives in, then work with RELATIVE paths.
# Rationale (B5-0306): the previous version computed absolute POSIX paths
# (/c/...) via `pwd` and passed them to the native Windows javac, which
# cannot read MSYS-style paths; CRLF line endings additionally broke
# stricter shells. Relative paths from the script directory need no
# translation and work under sh, bash, and MSYS/Git Bash alike.
cd "$(dirname "$0")" || exit 1

SRC="src"
OUT="out"
RES="resources"

echo "=== B5 CCG Build ==="
echo "Source: $(pwd)/$SRC"
echo "Output: $(pwd)/$OUT"

mkdir -p "$OUT"

# Collect all .java files (relative; word-split below on IFS incl. newlines)
JAVA_FILES=$(find "$SRC" -name "*.java")

if [ -z "$JAVA_FILES" ]; then
  echo "ERROR: No .java files found in $SRC"
  exit 1
fi

echo "Compiling $(echo "$JAVA_FILES" | wc -l | tr -d ' ') source files…"
# shellcheck disable=SC2086 — no spaces in source filenames in this repo
javac -source 6 -target 6 -encoding UTF-8 -d "$OUT" $JAVA_FILES

echo "Copying resources…"
if [ -d "$RES" ]; then
  cp -r "$RES"/. "$OUT"/
fi

echo ""
echo "✓ Build successful. Run with: ./run.sh"

# ── Optional verification gate (B5-0308 wiring) ────────────────────────────
# Run with: RUN_TESTS=1 sh compile.sh
# Executes the rulebook-conformance suite (B5-0308) and the headless smoke
# test (B5-0201) after a successful build; any failure exits non-zero so
# rule regressions are caught by the build. Default is off so a plain
# `sh compile.sh` stays a fast compile gate.
if [ "${RUN_TESTS:-0}" = "1" ]; then
  echo ""
  echo "=== Verification (RUN_TESTS=1) ==="
  java -cp "$OUT" b5ccg.engine.HeadlessConformanceTest
  java -cp "$OUT" b5ccg.engine.HeadlessSmokeTest
  echo "✓ Verification passed."
fi
