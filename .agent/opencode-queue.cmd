@echo off
REM opencode-queue.cmd -- run-queue.ps1 wrapper for opencode CLI, OmniRoute "me-so-poor" model.
REM Mirrors .agent/codex-queue.cmd: run-queue.ps1 passes AgentArgs as ONE argv token, so
REM multi-flag arguments ("run --auto -m ...") cannot go through -AgentArgs directly. This
REM wrapper carries the fixed flags; the runner appends the task prompt as the trailing
REM arg(s) via %%*.
REM
REM Flags:
REM   run            non-interactive one-shot, the opencode analogue of `codex exec`
REM   --auto         auto-approve permissions not explicitly denied -- the analogue of
REM                   codex's --approve-for-me. Without it a headless run stalls on the
REM                   first permission prompt and never terminates the iteration.
REM   -m <model>     provider/model. omniroute/omni-auto resolves to the model whose
REM                   display name in ~/.config/opencode/opencode.jsonc is "me-so-poor".
REM
REM Usage: powershell -NoProfile -ExecutionPolicy Bypass -File .agent/run-queue.ps1
REM   -AgentCli 'C:\temp\projects\Babylon5CCGGameDesign\.agent\opencode-queue.cmd' -MaxIterations 10
opencode run --auto -m omniroute/auto/cheap %*
exit /b %ERRORLEVEL%
