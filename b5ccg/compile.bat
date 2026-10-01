@echo off
REM Compile the B5 CCG Java project (Java 6 only, no external libraries)
setlocal enabledelayedexpansion

set ROOT=%~dp0
set SRC=%ROOT%src
set OUT=%ROOT%out
set RES=%ROOT%resources

echo === B5 CCG Build ===

if not exist "%OUT%" mkdir "%OUT%"

REM Collect all .java files
set JAVA_FILES=
for /r "%SRC%" %%f in (*.java) do set JAVA_FILES=!JAVA_FILES! "%%f"

if "!JAVA_FILES!"=="" (
  echo ERROR: No .java files found.
  exit /b 1
)

echo Compiling source files...
javac -source 6 -target 6 -encoding UTF-8 -d "%OUT%" !JAVA_FILES!
if errorlevel 1 (
  echo Compilation failed.
  exit /b 1
)

echo Copying resources...
if exist "%RES%" xcopy /E /Y /Q "%RES%\*" "%OUT%\"

echo.
echo Build successful. Run with: run.bat

REM ── Verification gate: parity with compile.sh (B5-0967, 2026-09-28) ──────
REM This script has NO native test branch BY DECISION: the test selection
REM lives only in compile.sh's RUN_TESTS=1 branch, so the gate list is
REM decided in exactly one file (B5-0956 owns which Headless classes gate).
REM The full verified build on this platform (every agent builds through
REM Git Bash) is:
REM   RUN_TESTS=1 sh compile.sh
REM (cmd.exe without a POSIX shell: `set RUN_TESTS=1` then `sh compile.sh`
REM  via Git Bash; a plain `compile.bat` remains the fast compile-only gate.)
REM Measured 2026-09-28: RUN_TESTS=1 sh compile.sh exits 0 end-to-end here.

endlocal
