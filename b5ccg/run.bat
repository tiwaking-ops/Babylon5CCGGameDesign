@echo off
set ROOT=%~dp0
set OUT=%ROOT%out

if not exist "%OUT%" (
  echo Project not built. Running compile.bat first...
  call "%ROOT%compile.bat"
)

java -cp "%OUT%" b5ccg.Main %*
