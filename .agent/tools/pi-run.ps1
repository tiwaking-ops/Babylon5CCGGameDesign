<#
.SYNOPSIS
  argv shim so .agent/run-queue.ps1 can drive `pi -p` headlessly.

.DESCRIPTION
  run-queue.ps1 invokes `& $AgentCli @argList` where argList is AgentArgs
  (ONE argv token) plus the prompt. That shape cannot express
  `pi -p --model orcarouter/auto --provider orcarouter <prompt>` (five
  tokens): passing them via -AgentArgs (even as an array, which
  stringifies with spaces into one token) arrives as a single word and
  pi misparses it (same class as the mimo-run.ps1 / kiro-cli-run.ps1 /
  vibe-run.ps1 shims). This shim carries the fixed leading tokens; the
  runner supplies only the prompt (AgentArgs left empty):

    powershell -NoProfile -ExecutionPolicy Bypass -File .agent/run-queue.ps1 `
      -AgentCli .agent/tools/pi-run.ps1 -MaxIterations 4 -MaxSeedInvocations 1

  MODEL AND PROVIDER SELECTION (for parallel lanes on different
  models): the runner splats AgentArgs as the FIRST token and the prompt
  after it, so -AgentArgs '<model> [<provider>]' selects them for that
  lane. The token keeps its inner spaces (it is ONE argv element, split
  here, not by the runner):

    ... -AgentCli .agent/tools/pi-run.ps1 -AgentArgs 'tencent/hy4-preview-free' ...
    ... -AgentCli .agent/tools/pi-run.ps1 -AgentArgs 'poor-pi omniroute' ...
    ... -AgentCli .agent/tools/pi-run.ps1 -AgentArgs 'nvidia/nemotron-3-ultra-550b-a55b nvidia' ...

  A lone model keeps the default provider. With no AgentArgs the
  defaults below (or $env:PI_RUN_MODEL / $env:PI_RUN_PROVIDER) are used.

  (AgentCli may need a repo-root-relative or absolute path depending on
  the caller's working directory.) -p/--print runs non-interactively
  (process prompt and exit). Exit code is pi's.
#>
param([Parameter(ValueFromRemainingArguments = $true)][string[]]$InputArgs)
$ErrorActionPreference = 'Stop'
# NOTE: orcarouter/free was tried first and returns 402
# free_quota_exhausted; orcarouter/auto (wallet billing) works.
$Model = $env:PI_RUN_MODEL
if (-not $Model) { $Model = 'orcarouter/auto' }
$Provider = $env:PI_RUN_PROVIDER
if (-not $Provider) { $Provider = 'orcarouter' }
$Prompt = $InputArgs
if ($InputArgs.Count -ge 2) {
  # run-queue passes AgentArgs as the first token and the prompt after
  # it, so a leading token is the model/provider selection, not prompt
  # text. A lone token is just the prompt under the defaults. The token
  # is "<model> [<provider>]": inner spaces survive because it arrives
  # as ONE argv element (the runner never splits AgentArgs).
  $sel = $InputArgs[0] -split '\s+', 2
  $Model = $sel[0]
  if ($sel.Count -ge 2 -and $sel[1]) { $Provider = $sel[1] }
  $Prompt = @($InputArgs[1..($InputArgs.Count - 1)])
}
# `--` ends option parsing so a prompt beginning with `-` is still read
# as the message (pi chat --help).
& pi -p --model $Model --provider $Provider -- @Prompt
exit $LASTEXITCODE
