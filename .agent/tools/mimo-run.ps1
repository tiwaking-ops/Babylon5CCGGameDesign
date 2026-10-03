<#
.SYNOPSIS
  argv shim so .agent/run-queue.ps1 can drive `mimo run` headlessly.

.DESCRIPTION
  run-queue.ps1 invokes `& $AgentCli @argList` where argList is AgentArgs
  (ONE argv token) plus the prompt. That shape cannot express
  `mimo run --yolo <prompt>` (three tokens), so `-AgentArgs 'run --yolo'`
  arrives as one token and mimo prints top-level help with exit 1.
  This shim carries the fixed leading tokens; the runner supplies only
  the prompt (AgentArgs left empty):

    powershell -NoProfile -ExecutionPolicy Bypass -File .agent/run-queue.ps1 `
      -AgentCli .agent/tools/mimo-run.ps1 -MaxIterations 3

  (AgentCli may need a repo-root-relative or absolute path depending on
  the caller's working directory.) Exit code is mimo's.
#>
$ErrorActionPreference = 'Stop'
& mimo run --yolo @args
exit $LASTEXITCODE
