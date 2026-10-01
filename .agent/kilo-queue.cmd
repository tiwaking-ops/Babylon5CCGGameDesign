@echo off
REM kilo-queue.cmd -- run-queue.ps1 wrapper for the Kilo Code CLI (@kilocode/cli).
REM Mirrors .agent/codex-queue.cmd, opencode-queue.cmd and copilot-queue.cmd: run-queue.ps1
REM passes AgentArgs as ONE argv token, so multi-flag arguments cannot go through
REM -AgentArgs directly. This wrapper carries the fixed flags; the runner appends the
REM task prompt as the trailing arg(s) via %%*.
REM
REM -m is MANDATORY here, unlike in the other wrappers. Kilo's default model is
REM google/gemini-3-pro-image, which this host has no subscription for -- an unpinned run
REM dies immediately with "You need to sign in to use this model." before doing any work.
REM
REM Model id note: the reference is kilo/kilo-auto/free -- provider prefix "kilo/" plus model
REM "kilo-auto/free". Verified against `kilo models`, whose 331 rows include
REM kilo/kilo-auto/{balanced,efficient,free,frontier,small}. Do not shorten this to
REM "kilo-auto/free"; kilo parses provider/model, so the unpinned form is read as a provider
REM named "kilo-auto" and fails with "Provider not found". Kilo is an opencode fork, so
REM this provider-prefix convention matches opencode's own provider/model format.
REM
REM   --auto   auto-approve permissions not explicitly denied -- the analogue of codex's
REM            --approve-for-me and opencode's --auto. Note ~/.config/kilo/kilo.jsonc also
REM            carries an explicit bash allowlist; --auto covers everything not listed.
REM Usage: powershell -NoProfile -ExecutionPolicy Bypass -File .agent/run-queue.ps1
REM   -AgentCli '.agent\kilo-queue.cmd' -MaxIterations 10
REM Note: inside a batch file `kilo` resolves through PATHEXT to the npm shim kilo.cmd.
REM PowerShell's own `kilo` resolves to kilo.ps1 instead, which re-parses argv -- invoke
REM this wrapper from the runner, not `kilo` from a PowerShell prompt.
kilo run --auto -m kilo/kilo-auto/free %*
exit /b %ERRORLEVEL%