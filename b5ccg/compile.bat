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
endlocal
