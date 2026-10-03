<#
.SYNOPSIS
  argv shim so .agent/run-queue.ps1 can drive `kiro-cli chat` headlessly.

.DESCRIPTION
  run-queue.ps1 invokes `& $AgentCli @argList` where argList is AgentArgs
  (ONE argv token) plus the prompt. That shape cannot express
  `kiro-cli chat --no-interactive -a <prompt>` (four tokens), so passing
  the leading tokens via -AgentArgs arrives as one token and kiro-cli
  misparses it (same class as the mimo-run.ps1 shim). This shim carries
  the fixed leading tokens; the runner supplies only the prompt
  (AgentArgs left empty):

    powershell -NoProfile -ExecutionPolicy Bypass -File .agent/run-queue.ps1 `
      -AgentCli .agent/tools/kiro-cli-run.ps1 -MaxIterations 3

  (AgentCli may need a repo-root-relative or absolute path depending on
  the caller's working directory.) Flags: --no-interactive runs without
  expecting user input; -a/--trust-all-tools lets the model run commands
  without confirmation (required for autonomous close-out: compile.bat,
  census scripts, ledger edits). Exit code is kiro-cli's.
#>
$ErrorActionPreference = 'Stop'
# Absolute path: kiro-cli.exe is not on PATH in a clean shell (it lives
# under %LOCALAPPDATA%\Kiro-Cli\), so a bare `kiro-cli` fails with
# CommandNotFoundException when the runner spawns a fresh process.
$KiroCli = Join-Path $env:LOCALAPPDATA 'Kiro-Cli\kiro-cli.exe'
& $KiroCli chat --no-interactive -a @args
exit $LASTEXITCODE
