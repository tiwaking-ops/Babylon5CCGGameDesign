<#
.SYNOPSIS
  argv shim so .agent/run-queue.ps1 can drive `vibe -p` headlessly.

.DESCRIPTION
  run-queue.ps1 invokes `& $AgentCli @argList` where argList is AgentArgs
  (ONE argv token) plus the prompt. That shape cannot express
  `vibe --trust --yolo -p <prompt>` (four tokens): -p alone would fit as
  a single AgentArgs token, but without --yolo tool approval follows the
  agent config and prompts, hanging a non-interactive run. This shim
  carries the fixed leading tokens; the runner supplies only the prompt
  (AgentArgs left empty):

    powershell -NoProfile -ExecutionPolicy Bypass -File .agent/run-queue.ps1 `
      -AgentCli .agent/tools/vibe-run.ps1 -MaxIterations 3

  (AgentCli may need a repo-root-relative or absolute path depending on
  the caller's working directory.) Flags: -p/--prompt runs in
  programmatic mode (send prompt, output response, exit); --yolo
  approves all tool calls without prompting; --trust skips the workspace
  trust prompt for this invocation. Exit code is vibe's.
#>
$ErrorActionPreference = 'Stop'
& vibe --trust --yolo -p @args
exit $LASTEXITCODE
