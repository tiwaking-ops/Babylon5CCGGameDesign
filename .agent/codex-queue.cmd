@echo off
REM codex-queue.cmd -- run-queue.ps1 wrapper for Codex CLI (human-approved 2026-09-29).
REM run-queue.ps1 passes AgentArgs as ONE argv token, so multi-flag 'exec -s ...' cannot go
REM through -AgentArgs directly. This wrapper carries the fixed flags; the runner appends
REM the task prompt as the trailing arg(s) via %%*.
REM Usage: powershell -NoProfile -ExecutionPolicy Bypass -File .agent/run-queue.ps1
REM   -AgentCli 'C:\temp\projects\Babylon5CCGGameDesign\.agent\codex-queue.cmd' -MaxIterations 10 -MaxSeedInvocations 1
codex exec --approve-for-me %*
exit /b %ERRORLEVEL%

