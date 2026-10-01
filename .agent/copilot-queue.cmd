@echo off
REM copilot-queue.cmd -- run-queue.ps1 wrapper for the GitHub Copilot CLI.
REM Mirrors .agent/codex-queue.cmd and .agent/opencode-queue.cmd: run-queue.ps1 passes
REM AgentArgs as ONE argv token, so multi-flag arguments cannot go through -AgentArgs
REM directly. This wrapper carries the fixed flags; the runner appends the task prompt as
REM the trailing arg(s) via %%*.
REM
REM Flag order is load-bearing, do not reorder:
REM   --allow-all-tools   Copilot's analogue of codex --approve-for-me and opencode --auto.
REM                       Copilot REFUSES non-interactive mode without it -- "required for
REM                       non-interactive mode" -- so a run without this flag does not
REM                       merely prompt, it fails.
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
copilot --allow-all-tools -p %*
exit /b %ERRORLEVEL%