<#
.SYNOPSIS
  B5-1475: detect FILENAME LOOKALIKE path components anywhere in the repository,
  not only under `.agent/HEARTBEATS/`.

.DESCRIPTION
  Why this tool exists. `.agent/HEARTBEATS/README.md` R7 (B5-1062) forbids any
  file in the heartbeat store from carrying a private-use or separator codepoint
  in its filename, and `validate-heartbeats.ps1` enforces it — but that tool is
  scoped to ONE directory tree (`.agent/HEARTBEATS`, `-Recurse`). The measured
  evidence in `docs/proposals/lookalike-filename-r5-exception-proposal.md` §2 is
  that 17 path components carry U+F03A (FULLWIDTH COLON) and **15 of the 17 are
  outside the heartbeat store**: 1 directory under `.agent/PATTERNS/` holding 13
  files, 14 reports under `.agent/REPORTS/`, and 1 heartbeat under
  `b5ccg/src/.agent/HEARTBEATS/`. A gate that cannot see them cannot stop the
  next instance either, so R7's enforcement stops at the directory boundary the
  instrument happens to have.

  Measured 2026-10-02 by this tool on a fresh walk, two components beyond the
  proposal's inventory of 17: two untracked root-level directories,
  `.c<U+F03A>/` (holding one nested report) and `C<U+F03A>tempb5ccg-harness/`.
  Both are empty or harness scratch outside every governed store. A census that
  reported only the number its source document predicted would have missed them.

  What a lookalike is, in one sentence: a codepoint that is LEGAL in a Windows
  filename, renders to the reader as `:` or `/`, produces a DIFFERENT stem for the
  SAME `agent_id`, resolves with no `_registry.json` entry, and is invisible to a
  `*.json` glob. So every tool that enumerates by glob silently skips it.

  This tool is DETECTION ONLY, and deliberately so. It writes nothing, renames
  nothing, deletes nothing, and moves nothing. The remediation half of the same
  proposal — the R5 exception that would authorise renaming the 17 components to
  their canonical sanitised spelling — is part (b), which the human explicitly
  deferred to a later ruling on 2026-09-30. Shipping the rename without that
  ruling would break 11 live ledger citations and destroy 1 of 2 non-identical
  report collisions, which is exactly the damage R5 exists to prevent. A detection
  tool with no repair path is the correct size for an uncontested task.

  Every finding is classified by TIER so a later, separately-claimed remediation
  knows what it may and may not touch:

    LIVE-STORE   - a heartbeat file in `.agent/HEARTBEATS/`. Under R6 a session may
                   not edit a foreign heartbeat, so even these are report-only.
    QUARANTINE   - already out of the live store under `_quarantine/`, per the
                   B5-0773 precedent (quarantine, never delete).
    FOREIGN-NS   - inside another agent's `.agent/PATTERNS/<id>/` namespace.
                   Readable by all, writable by nobody but the owner.
    RECORD       - a report or any other `.md` record. Renaming one breaks the
                   citations that name it.
    OUT-OF-STORE - everything else, e.g. the two root scratch directories. Not a
                   coordination store; reported for the census, no policy attaches.

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/detect-filename-lookalikes.ps1

.OUTPUTS
  Exit 0 = no lookalike component found (pass condition; empty finding list).
  Exit 1 = at least one lookalike component found, each printed with its full
           relative path, the codepoints, the tier, byte size, SHA-256 prefix and
           git tracked state.
  Exit 2 = the root is missing or unreadable, or git could not be consulted.
           Deliberately NOT 0: a census that could not read the tree must never
           report a clean tree. Same discipline as run-dup-census.ps1 and
           check-citations.ps1.
#>
[CmdletBinding()]
param(
    # Repository root to walk. Defaults to the repo this tool ships in.
    [string]$Root,
    # Skip the git tracked/untracked annotation (which costs one subprocess).
    [switch]$NoGit
)

$ErrorActionPreference = 'Stop'

if (-not $Root) { $Root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot) }
if (-not (Test-Path -LiteralPath $Root -PathType Container)) {
    Write-Output ("[B5-1475] EXIT 2: root is not a readable directory: {0}" -f $Root)
    exit 2
}

# Codepoint families, kept identical to validate-heartbeats.ps1 lines 197-199 so
# the two instruments cannot disagree about what a lookalike is:
#   BMP private use          U+E000-U+F8FF
#   Supplementary PUA        U+F0000-U+FFFFD, U+100000-U+10FFFD
#   Separators               U+2028 line sep, U+2029 para sep, U+00A0 NBSP
# Detection is done by Get-LookalikeCps below on integer codepoints, not by a regex:
# a regex literal for these families cannot be written in a .ps1 source file without
# embedding the very characters it forbids, which would make this tool its own
# finding. Codepoint comparison keeps the source ASCII-clean and auditable.

# Directories that cannot contain a governed artefact and are large. Skipping .git
# is not an optimisation only: it holds thousands of tracked paths.
$skipDirs = @('.git', 'node_modules')

$all = New-Object System.Collections.Generic.List[object]
$scanErrors = 0
$rootFull = (Resolve-Path -LiteralPath $Root).ProviderPath

function Get-LookalikeCps([string]$name) {
    $found = New-Object System.Collections.Generic.List[string]
    foreach ($c in $name.ToCharArray()) {
        $i = [int][char]$c
        $inBmpPua    = ($i -ge 0xE000 -and $i -le 0xF8FF)
        $inSuppPua1  = ($i -ge 0xF0000 -and $i -le 0xFFFFD)
        $inSuppPua2  = ($i -ge 0x100000 -and $i -le 0x10FFFD)
        $isSeparator = ($i -eq 0x2028 -or $i -eq 0x2029 -or $i -eq 0xA0)
        if ($inBmpPua -or $inSuppPua1 -or $inSuppPua2 -or $isSeparator) {
            $found.Add(('U+{0:X4}' -f $i))
        }
    }
    return $found
}

# Render a path with every lookalike codepoint spelled out as <U+XXXX>. The raw
# name is NOT printable evidence: this host's console codepage turns U+F03A into
# '?', so an unescaped path column reads as 'solar-pro4?free.json' -- the exact
# ambiguity this instrument exists to remove, reintroduced by its own output.
function Format-Safe([string]$rel) {
    $sb = New-Object System.Text.StringBuilder
    foreach ($c in $rel.ToCharArray()) {
        $i = [int][char]$c
        $inBmpPua    = ($i -ge 0xE000 -and $i -le 0xF8FF)
        $inSuppPua1  = ($i -ge 0xF0000 -and $i -le 0xFFFFD)
        $inSuppPua2  = ($i -ge 0x100000 -and $i -le 0x10FFFD)
        $isSeparator = ($i -eq 0x2028 -or $i -eq 0x2029 -or $i -eq 0xA0)
        if ($inBmpPua -or $inSuppPua1 -or $inSuppPua2 -or $isSeparator) {
            [void]$sb.Append(('<U+{0:X4}>' -f $i))
        } else {
            [void]$sb.Append($c)
        }
    }
    return $sb.ToString()
}

function Get-Tier([string]$rel) {
    $n = $rel -replace '\\', '/'
    if ($n -like '.agent/HEARTBEATS/_quarantine/*' -or $n -like '.agent/HEARTBEATS/_retired/*') { return 'QUARANTINE' }
    if ($n -like '.agent/HEARTBEATS/*')                                  { return 'LIVE-STORE' }
    if ($n -like '.agent/PATTERNS/*')                                    { return 'FOREIGN-NS' }
    if ($n -like '.agent/REPORTS/*')                                     { return 'RECORD' }
    return 'OUT-OF-STORE'
}

# Stack-based walk. Get-ChildItem -Recurse is avoided deliberately: a name holding
# a separator or a PUA codepoint is exactly the case where a provider path gets
# reinterpreted, and a walk driven by literal names keeps the codepoint intact.
$stack = New-Object System.Collections.Generic.Stack[string]
$stack.Push($rootFull)

while ($stack.Count -gt 0) {
    $dir = $stack.Pop()
    $entries = $null
    try {
        $entries = [System.IO.Directory]::GetFileSystemEntries($dir)
    } catch {
        Write-Output ("[B5-1475] WARNING: unreadable directory, not counted clean: {0} ({1})" -f $dir, $_.Exception.Message)
        $scanErrors++
        continue
    }
    foreach ($e in $entries) {
        $leaf = [System.IO.Path]::GetFileName($e)
        $cps  = Get-LookalikeCps $leaf
        $isDir = [System.IO.Directory]::Exists($e)
        if ($cps.Count -gt 0) {
            $rel = $e.Substring($rootFull.Length).TrimStart('\', '/') -replace '\\', '/'
            $all.Add([pscustomobject]@{
                Path     = $rel
                Cps      = ($cps -join ',')
                Tier     = (Get-Tier $rel)
                IsDir    = $isDir
                Bytes    = $(if ($isDir) { -1 } else { (New-Object System.IO.FileInfo($e)).Length })
                Sha12    = $(if ($isDir) { '' } else {
                                $sha = [System.Security.Cryptography.SHA256]::Create()
                                try {
                                    $fs = [System.IO.File]::OpenRead($e)
                                    try {
                                        ([System.BitConverter]::ToString($sha.ComputeHash($fs)).Replace('-', '')).Substring(0, 12)
                                    } finally { $fs.Dispose() }
                                } finally { $sha.Dispose() }
                            })
            })
        }
        if ($isDir -and ($skipDirs -notcontains $leaf)) { $stack.Push($e) }
    }
}

# git tracked/untracked, one subprocess for the whole tree. A component's tracked
# state is decision-relevant: a rename of a TRACKED file is a git-visible change
# to a cited record, while an UNTRACKED one is scratch nobody cites. It is
# annotation only; this tool never acts on it.
$tracked = $null
if (-not $NoGit) {
    $prevEncoding = [Console]::OutputEncoding
    try {
        [Console]::OutputEncoding = New-Object System.Text.UTF8Encoding($false)
        $tracked = New-Object System.Collections.Generic.HashSet[string]
        # [Console]::OutputEncoding is LOAD-BEARING here, and getting it wrong is
        # INVISIBLE. PowerShell decodes a native command's stdout with the console
        # output encoding; under this host's default cp1252 the three UTF-8 bytes of
        # U+F03A arrive as the three characters U+00B4 U+00C7 U+2551, so the tracked
        # set holds a mojibake path, the lookup silently misses, and EVERY lookalike
        # reports 'untracked' -- a plausible, green-looking, entirely wrong column.
        # Measured by isolating both candidate causes (B5-1475 second run): the
        # console encoding is the whole cause and git's core.quotePath is irrelevant
        # (quotePath true and false both report untracked under cp1252, and both
        # report tracked once the console is UTF-8). quotePath=false is retained
        # because it is the honest request; it is not what fixes it.
        $raw = & git -C $rootFull -c core.quotePath=false ls-files -z 2>$null
        if ($LASTEXITCODE -eq 0 -and $null -ne $raw) {
            foreach ($p in ($raw -split "`0")) {
                if ($p) { [void]$tracked.Add(($p -replace '\\', '/')) }
            }
        } else {
            Write-Output '[B5-1475] WARNING: git ls-files unavailable; tracked/untracked column omitted.'
            $tracked = $null
        }
    } catch {
        Write-Output ("[B5-1475] WARNING: git ls-files failed ({0}); tracked/untracked column omitted." -f $_.Exception.Message)
        $tracked = $null
    } finally {
        # Restore the caller's console encoding. A tool that leaves the session's
        # stdout encoding changed is a tool that silently corrupts the NEXT command
        # a human or another agent runs in the same shell.
        [Console]::OutputEncoding = $prevEncoding
    }
}

$rows = @($all | Sort-Object Path)
Write-Output ("[B5-1475] root        : {0}" -f $rootFull)
Write-Output ("[B5-1475] codepoint families: BMP PUA U+E000-U+F8FF, supplementary PUA U+F0000-U+FFFFD and U+100000-U+10FFFD, separators U+2028/U+2029/U+00A0")
Write-Output ("[B5-1475] components  : {0}" -f $rows.Count)
Write-Output ("[B5-1475] unreadable dirs: {0}" -f $scanErrors)

if ($rows.Count -gt 0) {
    Write-Output ''
    Write-Output ('{0,-4} {1,-12} {2,-10} {3,-8} {4,-14} {5}' -f 'DIR', 'TIER', 'BYTES', 'SHA-12', 'CODEPOINTS', 'PATH')
    foreach ($r in $rows) {
        # A directory has no git entry of its own; git tracks files. A lookalike
        # DIRECTORY is therefore 'tracked' when any tracked file sits inside it,
        # which is the question a remediation actually needs answered.
        $t = 'n/a'
        if ($null -ne $tracked) {
            if ($r.IsDir) {
                $prefix = $r.Path + '/'
                $t = $(if (@($tracked.Where({ $_ -like ($prefix + '*') })).Count -gt 0) { 'tracked(dir)' } else { 'untracked' })
            } else {
                $t = $(if ($tracked.Contains($r.Path)) { 'tracked' } else { 'untracked' })
            }
        }
        Write-Output ('{0,-4} {1,-12} {2,-10} {3,-8} {4,-14} {5}  [{6}]' -f
            $(if ($r.IsDir) { 'DIR' } else { 'file' }), $r.Tier, $r.Bytes, $r.Sha12, $r.Cps, (Format-Safe $r.Path), $t)
    }
    Write-Output ''
    foreach ($g in ($rows | Group-Object Tier | Sort-Object Name)) {
        Write-Output ("[B5-1475] {0,-12} {1}" -f $g.Name, $g.Count)
    }
    Write-Output ''
    Write-Output '[B5-1475] NO FILE WAS RENAMED, MOVED OR DELETED BY THIS TOOL.'
    Write-Output '[B5-1475] Remediation is part (b) of docs/proposals/lookalike-filename-r5-exception-proposal.md,'
    Write-Output '[B5-1475] which the human deferred to a later ruling on 2026-09-30. Until that ruling, every'
    Write-Output '[B5-1475] component above stays byte-identical (R5, R6).'
    exit 1
}

if ($scanErrors -gt 0) {
    Write-Output '[B5-1475] EXIT 2: at least one directory was unreadable, so the tree was NOT fully scanned.'
    exit 2
}
exit 0
