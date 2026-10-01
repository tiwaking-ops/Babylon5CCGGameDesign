<#
.SYNOPSIS
  Standing verification battery (B5-1019): one command that runs the six
  cold-boot instruments, prints each DESIGNED exit code next to the OBSERVED
  one, and returns a single GREEN/RED verdict.

.DESCRIPTION
  Assembles the 2026-09-29 cold-boot verification sequence that previously
  took six separate invocations and had no completeness receipt:

    1. javac -version                       toolchain, JDK 8 expected
    2. b5ccg\compile.bat                    build gate (javac -source 6)
    3. .agent\tools\run-dup-census.ps1      duplicate task ids (exit 0/1/2)
    4. .agent\tools\ledger-query.ps1 -Status "*"   row/pipe census
    5. .agent\tools\validate-heartbeats.ps1        heartbeat conformance
    6. .agent\run-queue.ps1 -DryRun         claimable census

  The battery REPORTS and does not ACT: it never repairs, never reaps, never
  edits any file, and never changes an instrument's exit code, threshold, TTL
  or verdict. Child instruments keep their own exit codes.

  EXPECTED-RED CONTRACT (the B5-1019 row's central requirement: a battery
  without a declared expected-red list trains every future session to ignore
  red). Standing state as of 2026-09-30, each with its measured reason:

    - validate-heartbeats: designed 0, standing observed 1. Declared reason:
      the out-of-enum tombstone "Cline (space-bunny) b5-0941.json"
      (state 'released') plus the legacy solar-pro4 / freebuff-NN lookalike
      class. All foreign; none battery-owned.

  THE EXPECTED RED IS A SET, NOT A NUMBER (B5-1067). B5-1019 declared the
  standing state as a scalar, and line 102 compared only the observed exit code
  against it. That is unsound for an instrument whose red is a COLLECTION: every
  regression that leaves the exit code at 1 reads GREEN. Measured, not assumed -
  a TEMP copy of the live store went from 9 to 14 non-conforming files while the
  exit code stayed 1, so the gate meant to certify the store never moved. The
  same held for identity collisions (3 becoming 4 also holds exit 1). So the
  declared state now also pins the exact expected inventory of non-conforming
  filenames and the exact expected set of colliding agent_ids, compared in BOTH
  directions. The scalar check is KEPT as a cheap fast path; the set check is
  strictly additive and can only report MORE, never fewer.

  Consequence to state plainly: the standing state is now load-bearing in a way
  a scalar was not. When the store legitimately changes, the battery goes RED
  and the inventory must be re-declared. That is the intended cost - a
  re-declaration is a receipt, whereas a silently-wrong GREEN is not - but it
  does mean a hot multi-agent tree can hold the battery red on a change that is
  somebody else's news.

  THE INSTRUMENT'S MACHINE OUTPUT IS CURRENTLY NOT MACHINE-READABLE.
  validate-heartbeats.ps1 emits its identity-collision banner (lines 252-260)
  OUTSIDE the JSON branch, so -Json output is a JSON array followed by prose and
  ConvertFrom-Json throws whenever a collision exists - which is exactly the
  state this battery exists to certify. The observed inventory is therefore
  sliced from the JSON array prefix, and ANY parse failure reads RED and never
  GREEN, following the instrument's own stated rule that a parse failure is a
  finding and never a crash. The instrument is NOT edited to fix this: it is
  another claim's surface.

  Everything not listed in $ExpectedRed is expected observed 0. A red on an
  instrument whose observed code matches its DECLARED standing state is a
  GREEN battery (standing state reproduced, not silently ignored - the
  designed code and the reason still print). A red for an UNDECLARED reason
  flips the battery RED.

  -SelfTest proves the RED path end-to-end TWICE, because one proof per exit
  contract is not coverage of two different verdicts. Proof 1 drives
  run-dup-census.ps1 at a synthetic duplicate-ID ledger in %TEMP% (the instrument
  takes -LedgerPath precisely for this) and asserts the battery verdict RED on
  the scalar path. Proof 2 (B5-1067) copies the heartbeat store to %TEMP%,
  injects broken files into the LIVE tier, and asserts the battery verdict RED
  on the SET path - the one B5-1019's single proof never exercised, and the one
  that actually failed. No live file is modified by either. A battery that
  cannot go red is an ornament, not a gate; a battery with only one red path is
  a gate with one untested door.

  Exit codes: 0 = GREEN, 1 = RED, 2 = the battery could not run (layout
  broken or an instrument missing). Distinct from GREEN by design (the
  run-dup-census 0/1/2 lesson): a battery that could not run must never
  report as a pass.

.NOTES
  Filing history: first draft of this file was discarded uncommitted after
  self-review found a hallucinated include and a dead self-test stub; this
  version is the B5-1019 deliverable. Supersede-never-rewrite applies to
  REPORTS and PATTERNS, not to an in-flight same-claim rewrite of a file
  this claim created minutes earlier.

#>
[CmdletBinding()]
param(
  [switch]$SelfTest,
  # B5-1067: the SET-path red proof. Separate from -SelfTest on purpose: each
  # proof must be runnable alone, so a reader can see one red path green up on
  # its own without the other's verdict masking it.
  [switch]$SelfTestSet,
  # B5-1067: internal. Redirects ONLY the heartbeat instrument's -Directory so the
  # set self-test can drive a TEMP copy of the store. Default is the live store,
  # which is what every ordinary invocation uses.
  [string]$HeartbeatDirectory
)

$ErrorActionPreference = 'Continue'
$batteryVersion = 'B5-1019 + B5-1067 2026-09-30'
$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)

# ---- B5-1067: pin the pipe to UTF-8 BEFORE any native invocation ----------
# This host's console codepage is ibm850, and on ibm850 a non-ASCII filename is
# DESTROYED in transit through a native command's stdout: the store's
# 'solar-pro4<U+F03A>free.json' arrives as 'solar-pro4?free.json', the codepoint
# gone, irreversibly and silently. Measured both ways on 2026-09-30 - the same
# instrument, same run, default codepage 850 loses U+F03A, forced UTF-8 keeps it.
# That matters here because an exact-string inventory comparison is only as
# trustworthy as the strings it compares, and this store is FULL of U+F03A names
# (the 17-component B5-1066 manifest). A set gate that silently compared
# 'solar-pro4?free.json' against 'solar-pro4<U+F03A>free.json' would report a
# permanent unsatisfiable divergence - a gate red for a reason no session can
# ever fix, which is the "trains its readers to ignore red" failure again.
# Not cosmetic: a lost codepoint is also a lost agent_id, since the lookalike
# exists precisely to produce a different stem for the same identity.
$script:encodingPinned = $false
try {
  [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
  $script:encodingPinned = $true
} catch {
  # Deliberately NOT fatal. Failing to pin the encoding is a finding the set gate
  # will surface as a divergence, and a battery that exits 2 here would report a
  # layout break for a condition it can still measure.
  Write-Warning ("could not pin console output encoding to UTF-8: " + $_.Exception.Message)
}

# ---- expected-red contract: instrument -> declared standing observed code --
# B5-1067: the declared state is a SET, not only a code. StandingObserved stays
# as the cheap fast path; StandingNonConforming and StandingCollisions are the
# load-bearing part, because validate-heartbeats exits 1 for ">=1 non-conforming"
# AND for ">=1 collision", so every count regression holds the code at 1.
$ExpectedRed = @{}
$ExpectedRed['validate-heartbeats'] = @{
  StandingObserved = 1
  Reason = 'declared 2026-09-30: out-of-enum tombstone Cline (space-bunny) b5-0941.json (state released) plus legacy solar-pro4/freebuff-NN lookalike class; all foreign, none battery-owned'
  # Measured 2026-09-30T05:47Z by slicing the instrument's own -Json array prefix.
  # 8 of these 9 are inside _quarantine/ (B5-1062 put them there by decision);
  # exactly 1 is in the live tier, the Cline tombstone. Re-declare, do not
  # silently absorb, when this inventory legitimately changes.
  StandingNonConforming = @(
    'Cline (space-bunny) b5-0941.json',
    'freebuff-01.json',
    'freebuff-02.json',
    'freebuff-03.json',
    'kiro-pi.timestamp',
    'me-so-poor.json.bak',
    'solar-pro4',
    'solar-pro4.json',
    # The U+F03A lookalike is BUILT FROM ITS CODEPOINT, never pasted as a literal.
    # A raw private-use character in a source file renders as '?' or a box, so
    # every reader of this line and every tool that rewrites the file can silently
    # turn the declaration into a different filename - and a set predicate that
    # compares the wrong string fails OPEN, which is the one direction a
    # verification gate must never fail. This is also the exact defect class
    # DECISIONS B5-1001 is blocked on.
    ('solar-pro4' + [char]0xF03A + 'free.json')
  )
  # Mirrors validate-heartbeats.ps1 lines 203-210: one agent_id, >1 file.
  StandingCollisions = @(
    'Buffy (deepseek-v4-flash)',
    'me-so-poor',
    'solar-pro4:free'
  )
}

# ---- layout gate: exit 2, never a false pass -------------------------------
$required = @(
  (Join-Path $repoRoot '.agent\tools\run-dup-census.ps1'),
  (Join-Path $repoRoot '.agent\tools\ledger-query.ps1'),
  (Join-Path $repoRoot '.agent\tools\validate-heartbeats.ps1'),
  (Join-Path $repoRoot '.agent\tools\census-crosscheck.ps1'),
  (Join-Path $repoRoot '.agent\run-queue.ps1'),
  (Join-Path $repoRoot 'b5ccg\compile.bat'),
  (Join-Path $repoRoot 'b5ccg\compile.sh')
)
$missing = @($required | Where-Object { -not (Test-Path -LiteralPath $_) })
if ($missing.Count -gt 0) {
  Write-Error ("battery layout check failed, missing: " + ($missing -join ', '))
  exit 2
}

# ---- shared result core -----------------------------------------------------
$script:Results = New-Object System.Collections.Generic.List[object]

function Test-Instrument {
  param(
    [string]$Name,
    [int]$Designed,
    [scriptblock]$Probe,
    [string]$Detail,
    # B5-1067: optional. Returns the list of DIVERGENCES between the observed
    # inventory and the declared standing inventory. Empty = at the standing
    # state. Any non-empty result forces RED even when the scalar code matched,
    # because the scalar cannot see inside its own red.
    [scriptblock]$SetGate
  )
  $observed = & $Probe
  if ($null -eq $observed) { $observed = -1 }     # UNKNOWN is never 0, never a pass
  $verdict = 'GREEN'
  $declared = $ExpectedRed.ContainsKey($Name)
  if ($declared) {
    if ($observed -ne $ExpectedRed[$Name].StandingObserved) { $verdict = 'RED' }
  } elseif ($observed -ne 0) {
    $verdict = 'RED'
  }
  $divergence = @()
  if ($SetGate) { $divergence = @(& $SetGate | Where-Object { $_ }) }
  if ($divergence.Count -gt 0) { $verdict = 'RED' }
  $script:Results.Add([pscustomobject]@{
    Name = $Name; Designed = $Designed; Observed = $observed
    Verdict = $verdict; Declared = $declared; Detail = $Detail
    Divergence = $divergence
  })
  return $observed
}

function Get-HeartbeatInventory {
  <#
    B5-1067. Reads the heartbeat instrument's own machine output and returns the
    observed inventory: which files are non-conforming, and which agent_ids
    collide. It does NOT re-derive schema validation - a second implementation
    of the rules would be a second thing to be wrong, and the whole point is to
    read what the instrument actually concluded.

    Handles the instrument's current -Json defect: the identity-collision
    banner is written outside the JSON branch, so stdout is a JSON array
    followed by prose and ConvertFrom-Json throws. The array prefix is sliced
    off at the first '===' line. Every failure path returns Readable = $false,
    which the set gate turns into RED - never GREEN.
  #>
  param([Parameter(Mandatory = $true)][string]$Directory)
  $inv = @{ Readable = $false; Rows = 0; NonConforming = @(); Collisions = @(); Exit = -1; Error = '' }
  $tool = Join-Path $repoRoot '.agent\tools\validate-heartbeats.ps1'
  $raw  = (& powershell -NoProfile -ExecutionPolicy Bypass -File $tool -Directory $Directory -Json 2>$null | Out-String)
  $inv.Exit = $LASTEXITCODE
  $lines  = $raw -split "`r?`n"
  $banner = ($lines | Select-String -Pattern '^===' | Select-Object -First 1).LineNumber
  if ($banner -and $banner -le 1) { $inv.Error = 'no JSON array before the collision banner'; return $inv }
  $prefix = if ($banner) { ($lines[0..($banner - 2)] -join "`n") } else { ($lines -join "`n") }
  try {
    $data = $prefix | ConvertFrom-Json -ErrorAction Stop
  } catch {
    $inv.Error = $_.Exception.Message.Split([char]10)[0]
    return $inv
  }
  # PS 5.1 ConvertFrom-Json emits a top-level JSON ARRAY as ONE pipeline object,
  # so filtering inside the pipeline (ConvertFrom-Json | Where-Object) hands the
  # whole array through as a single row: Rows = 1 and every $_.Verdict is an
  # Object[] of all verdicts. Normalise AFTER assignment instead. Also covers the
  # empty-store case, where ConvertFrom-Json returns $null and @($null) is a
  # 1-element array - an empty store would otherwise read as one conforming row.
  if ($null -eq $data) { $data = @() } else { $data = @($data) }
  $inv.Readable  = $true
  $inv.Rows      = $data.Count
  $inv.NonConforming = @($data | Where-Object { $_.Verdict -ne 'CONFORMS' } | ForEach-Object { $_.File } | Sort-Object)
  # Mirrors validate-heartbeats.ps1 lines 203-210: one agent_id, more than one file.
  $inv.Collisions = @($data | Where-Object { -not [string]::IsNullOrEmpty($_.AgentId) } |
                        Group-Object AgentId | Where-Object { $_.Count -gt 1 } |
                        ForEach-Object { $_.Name } | Sort-Object)
  return $inv
}

function Get-HeartbeatSetGate {
  <# B5-1067. Compares the observed inventory against the declared one, BOTH
     directions. -notcontains is case-insensitive, which is what a Windows
     filename comparison requires; a case-sensitive comparison would let
     Solar-Pro4.json pass as solar-pro4.json and fail OPEN. #>
  $inv = $script:hbInv
  if ($null -eq $inv) { return @('heartbeat instrument was never run - no inventory to compare') }
  if (-not $inv.Readable) {
    return @('heartbeat machine output unparseable (' + $inv.Error + ') - a parse failure is a finding, never a pass')
  }
  $d       = @()
  if (-not $script:encodingPinned) {
    $d += ('console output encoding is NOT pinned to UTF-8, so non-ASCII filenames in this store cannot be compared reliably; every set comparison below is suspect')
  }
  $declNC  = @($ExpectedRed['validate-heartbeats'].StandingNonConforming)
  $obsNC   = @($inv.NonConforming)
  $declCol = @($ExpectedRed['validate-heartbeats'].StandingCollisions)
  $obsCol  = @($inv.Collisions)
  foreach ($f in @($obsNC  | Where-Object { $declNC  -notcontains $_ })) { $d += ('NEW non-conforming file, not in the declared inventory: [' + $f + ']') }
  foreach ($f in @($declNC | Where-Object { $obsNC   -notcontains $_ })) { $d += ('declared non-conforming file is now CONFORMING, re-declare the inventory: [' + $f + ']') }
  foreach ($c in @($obsCol  | Where-Object { $declCol -notcontains $_ })) { $d += ('NEW colliding agent_id, not in the declared inventory: [' + $c + ']') }
  foreach ($c in @($declCol | Where-Object { $obsCol  -notcontains $_ })) { $d += ('declared colliding agent_id has RESOLVED, re-declare the inventory: [' + $c + ']') }
  $d
}

function Show-Verdict {
  Write-Host ""
  $fmt = '{0,-30} {1,8} {2,8} {3,8} {4}'
  Write-Host ($fmt -f 'INSTRUMENT', 'DESIGNED', 'OBSERVED', 'VERDICT', 'DECLARED-RED')
  foreach ($r in $script:Results) {
    Write-Host ($fmt -f $r.Name, $r.Designed, $r.Observed, $r.Verdict, $(if ($r.Declared) { 'yes' } else { '-' }))
    if ($r.Detail) { Write-Host ("    " + $r.Detail) }
    # B5-1067: name every divergence. A RED that says only "off its declared
    # code" is a RED the next session has to re-derive from scratch, which is
    # the same cost that made the 10 pipe-count false positives permanent.
    foreach ($d in @($r.Divergence)) { Write-Host ("    DIVERGENCE: " + $d) }
  }
  $reds = @($script:Results | Where-Object { $_.Verdict -eq 'RED' })
  if ($reds.Count -eq 0) {
    Write-Host "=== BATTERY GREEN: every instrument at its designed-or-declared code ==="
    return 0
  }
  Write-Host ("=== BATTERY RED: " + $reds.Count + " instrument(s) off their designed-or-declared code: " + (($reds | ForEach-Object { $_.Name }) -join ', '))
  Write-Host "    the battery reports and does not act; see instrument outputs above"
  return 1
}

# =================================================================================
if ($SelfTest) {
  # RED-path proof: a synthetic duplicate-ID ledger in %TEMP%, judged by the
  # real instrument at its real exit contract (0 clean / 1 duplicate / 2 unreadable).
  $fix = Join-Path ([System.IO.Path]::GetTempPath()) ('b5-battery-selftest-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
  New-Item -ItemType Directory -Path $fix -Force | Out-Null
  $fixLedger = Join-Path $fix 'TASK_LEDGER.md'
  $row = '| B5-9001 | OPEN | self-test row A | - | - |'
  $dup = '| B5-9001 | OPEN | self-test row B (duplicate id) | - | - |'
  [System.IO.File]::WriteAllText($fixLedger, ($row + "`r`n" + $dup + "`r`n"), [System.Text.UTF8Encoding]::new($false))
  Write-Host ("=== B5 verification battery " + $batteryVersion + " - SELF-TEST (RED path) ===")
  & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repoRoot '.agent\tools\run-dup-census.ps1') -LedgerPath $fixLedger | Out-Null
  Test-Instrument -Name 'run-dup-census (selftest fixture)' -Designed 0 -Detail 'duplicate-ID fixture in %TEMP%; designed 0, expected observed 1 = the instrument detecting the duplicate' -Probe {
    $LASTEXITCODE
  }.GetNewClosure() | Out-Null
  Remove-Item -LiteralPath $fix -Recurse -Force -ErrorAction SilentlyContinue
  $code = Show-Verdict
  exit $code
}

# =================================================================================
if ($SelfTestSet) {
  # B5-1067 RED-path proof 2: the SET gate. Proof 1 above exercises the scalar
  # path; this one exercises the path that actually failed, on a TEMP copy of the
  # live store so no live file is touched. It injects broken files into the LIVE
  # tier (not _quarantine/) and asserts the battery reads RED on the divergence.
  Write-Host ("=== B5 verification battery " + $batteryVersion + " - SELF-TEST (SET path) ===")
  $copy = Join-Path ([System.IO.Path]::GetTempPath()) ('b5-battery-settest-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
  Copy-Item -LiteralPath (Join-Path $repoRoot '.agent\HEARTBEATS') -Destination $copy -Recurse -Force
  # Unparseable content: the instrument records a PARSE FAILED finding, and the
  # file is plainly in the live tier, so this is exactly the regression the old
  # scalar-only gate could not see - the exit code stays 1 either way.
  foreach ($n in 1..5) {
    [System.IO.File]::WriteAllText((Join-Path $copy ('INJECTED-BROKEN-' + $n + '.json')), '{ this is not json', [System.Text.UTF8Encoding]::new($false))
  }
  $script:hbInv = Get-HeartbeatInventory -Directory $copy
  # The fixture is registered under the REAL declared name, not a fixture name.
  # That is the whole point of a differential proof: the scalar check now sees
  # observed 1 against declared standing 1 and passes, exactly as it would have
  # in production, so the ONLY thing that can turn this RED is the set gate. A
  # proof that used a name absent from $ExpectedRed would have gone RED on the
  # scalar branch too and would therefore prove nothing about the defect.
  Test-Instrument -Name 'validate-heartbeats' -Designed 0 `
    -Detail 'TEMP copy of the live store + 5 broken files in the LIVE tier. The exit code is UNCHANGED at 1, which equals the declared standing observed code, so the scalar check alone passes.' `
    -SetGate { Get-HeartbeatSetGate } -Probe { $script:hbInv.Exit } | Out-Null
  Write-Host ("    fixture inventory: " + $script:hbInv.Rows + " rows, " + @($script:hbInv.NonConforming).Count + " non-conforming, " + @($script:hbInv.Collisions).Count + " colliding agent_id")
  # State the differential explicitly, and refuse to claim the proof if it does
  # not hold. A self-test that cannot tell whether it is testing the right thing
  # is the dead-stub failure B5-1019 already discarded one of.
  $scalarOnlyGreen = ($script:hbInv.Exit -eq $ExpectedRed['validate-heartbeats'].StandingObserved)
  Write-Host ("    differential: observed " + $script:hbInv.Exit + " vs declared " + $ExpectedRed['validate-heartbeats'].StandingObserved + " -> scalar-only verdict would be " + $(if ($scalarOnlyGreen) { 'GREEN' } else { 'RED' }))
  if (-not $scalarOnlyGreen) {
    Write-Warning 'SET PROOF INVALID: the scalar check alone would already be RED, so this run does not isolate the set gate'
  } else {
    Write-Host '    differential holds: scalar-only GREEN, so the RED below is attributable to the set gate alone'
  }
  $code = Show-Verdict
  Remove-Item -LiteralPath $copy -Recurse -Force -ErrorAction SilentlyContinue
  exit $code
}

# =================================================================================
# The six instruments, in boot order.
# =================================================================================
Write-Host ("=== B5 verification battery " + $batteryVersion + " ===")
Write-Host ("repo root: " + $repoRoot)
Write-Host ("encoding : console output pinned UTF-8 = " + $script:encodingPinned + " (receipt: non-ASCII filenames survive native stdout)")

# 1. toolchain: JDK 8 with the Java-6 source gate
Test-Instrument -Name 'javac -version' -Designed 0 -Detail 'JDK 8 expected (00_BOOT step 3)' -Probe {
  # stream merge happens inside cmd so PowerShell does not surface javac's stderr banner as an ErrorRecord
  $out = (& cmd /c "javac -version 2>&1" | Out-String).Trim()
  $script:javacLine = ($out -split "`n" | Where-Object { $_ -match 'javac ' } | Select-Object -First 1).Trim()
  if ($LASTEXITCODE -eq 0 -and $out -match '1\.8\.') { 0 } else { 1 }
} | Out-Null
Write-Host ("    javac: " + $script:javacLine)

# 2. build gate (the slow one; its stdout is the build's business, not ours)
Test-Instrument -Name 'b5ccg compile.bat' -Designed 0 -Detail 'javac -source 6 -target 6, stdlib only' -Probe {
  # compile.bat self-locates via %~dp0 (line 5), so invoke it by ABSOLUTE path:
  # no cwd semantics matter. The first draft moved the directory instead and
  # cmd never found the file - two different cwd mechanisms, both ignored.
  $bat = Join-Path $repoRoot 'b5ccg\compile.bat'
  & cmd /c "`"$bat`"" | Out-Null
  $LASTEXITCODE
} | Out-Null

# 3. duplicate-id census: 0 clean / 1 duplicate / 2 unreadable (B5-0777 contract)
Test-Instrument -Name 'run-dup-census' -Designed 0 -Detail 'exit contract 0/1/2; 2 must never read as a pass' -Probe {
  & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repoRoot '.agent\tools\run-dup-census.ps1') | Out-Null
  $LASTEXITCODE
} | Out-Null

# 4. ledger row/pipe census: exit 0 = the walk completed; defect counts live in columns
Test-Instrument -Name 'ledger-query -Status *' -Designed 0 -Detail 'every healthy row reads 7 pipes / doubleLead no' -Probe {
  & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repoRoot '.agent\tools\ledger-query.ps1') -Status "*" | Out-Null
  $LASTEXITCODE
} | Out-Null

# 5. heartbeat conformance + identity: designed 0, standing observed 1 (declared above)
# B5-1067: the exit code alone cannot see inside its own red, so the observed
# inventory is read from the instrument's machine output and compared to the
# declared inventory in both directions.
$script:hbDir = if ($HeartbeatDirectory) { $HeartbeatDirectory } else { Join-Path $repoRoot '.agent\HEARTBEATS' }
Test-Instrument -Name 'validate-heartbeats' -Designed 0 -Detail $ExpectedRed['validate-heartbeats'].Reason `
  -SetGate { Get-HeartbeatSetGate } -Probe {
    $script:hbInv = Get-HeartbeatInventory -Directory $script:hbDir
    $script:hbInv.Exit
  } | Out-Null
if ($script:hbInv -and $script:hbInv.Readable) {
  Write-Host ("    observed inventory: " + $script:hbInv.Rows + " rows, " + @($script:hbInv.NonConforming).Count + " non-conforming, " + @($script:hbInv.Collisions).Count + " colliding agent_id")
}

# 6. claimable census: exit 0 = the offer walk completed; a drained queue is a STATE, not an error
Test-Instrument -Name 'run-queue -DryRun' -Designed 0 -Detail 'drained queue is a statement about claims, not an error' -Probe {
  & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repoRoot '.agent\run-queue.ps1') -DryRun | Out-Null
  $LASTEXITCODE
} | Out-Null

# Completeness bonus (not one of the six): designed 0 with an expected DIVERGENT footer.
Test-Instrument -Name 'census-crosscheck (bonus)' -Designed 0 -Detail 'DIVERGENT 1 of 549 (B5-1022 shape) is an expected finding; nonzero exit is not' -Probe {
  & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repoRoot '.agent\tools\census-crosscheck.ps1') 2>$null | Out-Null
  $LASTEXITCODE
} | Out-Null

$code = Show-Verdict
exit $code
