@echo off
REM copilot-queue.cmd -- run-queue.ps1 wrapper for the GitHub Copilot CLI.
REM Mirrors .agent/codex-queue.cmd and .agent/opencode-queue.cmd: run-queue.ps1 passes
REM AgentArgs as ONE argv token, so multi-flag arguments cannot go through -AgentArgs
REM directly. This wrapper carries the fixed flags; the runner appends the task prompt as
REM the trailing arg(s) via %%*.
REM
REM Flag order is load-bearing, do not reorder:
REM
REM   --yolo              Enable all permissions = --allow-all-tools + --allow-all-paths.
REM                       --allow-all-tools ALONE IS NOT ENOUGH and fails the whole run:
REM                       the compile gate dies with
REM                         "Permission denied and could not request permission from user.
REM                          Copilot needed the user's approval and no one could answer."
REM                       Path verification is a SEPARATE gate from tool approval, and a
REM                       relative .bat invocation (`cmd /c .\b5ccg\compile.bat`) is
REM                       stopped by it. Copilot calls this "not an OS or sandbox error" --
REM                       it is the CLI's own permission layer.
REM                       Verified matrix, all four arms run against the real compile gate:
REM                         --allow-all-tools                      DENIED
REM                         --allow-all-tools + COPILOT_ALLOW_ALL=true DENIED
REM                         --allow-tool=shell (no --allow-all-tools) DENIED
REM                         --allow-all-tools --allow-all-paths     WORKS
REM                         --yolo                                  WORKS
REM                       --yolo is preferred over --allow-all because --allow-all also
REM                       grants --allow-all-urls, which no ledger task needs.
REM   -p %*               %* must come last. -p takes the prompt AS ITS VALUE, so a flag
REM                       placed after -p is swallowed as prompt text instead of being
REM                       parsed as a flag. Verified: `copilot -p --allow-all-tools "x"`
REM                       exits 1 with "a value is required for '--prompt <text>'".
REM
REM Model: unset, so Copilot's own routing picks (its `--model auto` default). Append
REM `--model <name>` BEFORE the `-p` to pin one.
REM Usage: powershell -NoProfile -ExecutionPolicy Bypass -File .agent/run-queue.ps1
REM   -AgentCli '.agent\copilot-queue.cmd' -MaxIterations 10
REM Note: inside a batch file `copilot` resolves through PATHEXT to the npm shim
REM copilot.cmd. PowerShell's own `copilot` instead resolves to copilot.ps1, which
REM re-parses argv and drops flags -- always invoke this wrapper from the runner, not
REM `copilot` from a PowerShell prompt.
copilot --yolo -p %*
exit /b %ERRORLEVEL%