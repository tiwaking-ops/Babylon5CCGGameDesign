# Measure Task-cell weights for rows after B5-1471 proposal date (2026-09-30)
#
# B5-2079. EXIT-CODE CONTRACT (added here; this tool previously had none, which is
# how B5-1730 could report a green verdict while measuring zero rows):
#   0 = MEASURED. The run produced a non-zero Count. This is NOT a statement that the
#       wave complies -- read the Count, then the VERDICT line. Exit 0 is deliberately
#       NOT reserved for compliance, because the live ledger is currently
#       non-compliant and reserving 0 for compliance would turn every honest run red.
#   2 = NOT MEASURED. Either the ledger was missing or unreadable, or it contained zero
#       rows in this wave. This is a FALSE PASS and must never be read as clean --
#       the same discipline as run-dup-census.ps1, check-citations.ps1 and
#       detect-bare-powershell-cmdlet.ps1.
# Before this change, the zero-row case threw three "Cannot index into a null array"
# errors and then printed "VERDICT: This wave FULLY COMPLIES" while exiting 0.
[CmdletBinding()]
param(
    [string]$LedgerPath = ".agent/TASK_LEDGER.md",
    [switch]$SelfTest
)

if ($SelfTest) {
    # Regression guard. Feeds the tool the two fixture classes it got wrong:
    # (1) a ledger with zero rows in the wave -- the B5-1730 false green; and
    # (2) synthetic five-digit and suffixed ids -- the four-digit-width defect
    # B5-2015 recorded, already repaired on disk but with nothing to stop it
    # coming back (a test never observed red is not evidence, AGENT_LOOP).
    $tmp = Join-Path ([System.IO.Path]::GetTempPath()) ('b52079-selftest-' + [Guid]::NewGuid().ToString('N').Substring(0, 8))
    New-Item -ItemType Directory -Path $tmp -Force | Out-Null
    $fail = 0
    try {
        $empty = Join-Path $tmp 'empty-ledger.md'
        [System.IO.File]::WriteAllText($empty, "| a | b |`r`nno rows here at all`r`n")
        $emptyOut = & powershell -NoProfile -ExecutionPolicy Bypass -File $PSCommandPath -LedgerPath $empty 2>&1 | Out-String
        $emptyRc = $LASTEXITCODE
        Write-Output ("[B5-2079] fixture 1 (zero rows): exit {0}, expected 2" -f $emptyRc)
        if ($emptyRc -ne 2) { $fail++ }
        if ($emptyOut -match 'VERDICT: This wave FULLY COMPLIES') {
            Write-Output "[B5-2079] fixture 1 FAILED: a green verdict on zero measured rows"
            $fail++
        }
        if ($emptyOut -match 'Cannot index into a null array') {
            Write-Output "[B5-2079] fixture 1 FAILED: null-array crash still reachable"
            $fail++
        }

        # Fixture 2: ids wider than four digits, and suffixed ids, must all be
        # captured and must not throw on the int cast.
        $wide = Join-Path $tmp 'wide-ledger.md'
        [System.IO.File]::WriteAllText($wide, @(
            '| B5-1472 | OPEN | aaaa | scope | - | - |',
            '| B5-1472a | OPEN | bbbb | scope | - | - |',
            '| B5-10000 | OPEN | cccc | scope | - | - |',
            '| B5-12345 | OPEN | dddd | scope | - | - |',
            '| B5-1471 | OPEN | eeee | scope | - | - |'
        ) -join "`r`n")
        $wideOut = & powershell -NoProfile -ExecutionPolicy Bypass -File $PSCommandPath -LedgerPath $wide 2>&1 | Out-String
        $wideRc = $LASTEXITCODE
        Write-Output ("[B5-2079] fixture 2 (wide and suffixed ids): exit {0}, expected 0" -f $wideRc)
        if ($wideRc -ne 0) { $fail++ }
        if ($wideOut -notmatch 'Count: 4') {
            Write-Output "[B5-2079] fixture 2 FAILED: expected Count: 4 (B5-1471 excluded, the other four captured)"
            $fail++
        }
        foreach ($want in @('B5-10000', 'B5-12345', 'B5-1472a')) {
            if ($wideOut -notmatch [regex]::Escape($want)) {
                Write-Output ("[B5-2079] fixture 2 FAILED: {0} was not captured" -f $want)
                $fail++
            }
        }
    } finally {
        Remove-Item -LiteralPath $tmp -Recurse -Force -ErrorAction SilentlyContinue
    }
    if ($fail -eq 0) {
        Write-Output "[B5-2079] SELF-TEST PASS: both fixtures behaved as required."
        exit 0
    }
    Write-Output ("[B5-2079] SELF-TEST FAIL: {0} check(s) failed." -f $fail)
    exit 1
}

# Row regex to extract id and Task cell (3rd pipe-delimited field after id)
# Format: | B5-XXXX | STATUS | TASK | SCOPE | VERIFIED | OWNER | DATE
$rowRegex = [regex]'\|+\s*(B5-\d+\w?)\s*\|\s*([^|]*?)\s*\|\s*([^|]*)\s*\|'

if (-not (Test-Path -LiteralPath $LedgerPath)) {
    Write-Output ("[B5-2079] NOT MEASURED: ledger not found or unreadable: {0}" -f $LedgerPath)
    Write-Output "VERDICT: NOT MEASURED -- this is NOT a pass. Exit 2."
    exit 2
}

$lines = (Get-Content $LedgerPath -Raw -Encoding UTF8) -split "`n"

$results = @()
foreach ($line in $lines) {
    $m = $rowRegex.Match($line)
    if (-not $m.Success) { continue }

    $id = $m.Groups[1].Value.Trim()
    $taskCell = $m.Groups[3].Value.Trim()
    $taskLen = $taskCell.Length

    # Check if row is after B5-1471 (proposal date 2026-09-30)
    # B5-1471 is the proposal, so IDs > 1471 (1473, 1475, 1477, 1479, 1523, 1525, 1527, etc.)
    $num = $id.Substring(3)
    # suffix-tolerant: drop trailing alpha (e.g. B5-2079a) before int-cast
    $numClean = $num -replace '[a-zA-Z]+$',''
    if ([int]$numClean -gt 1471) {
        $results += [pscustomobject]@{
            Id = $id
            TaskLen = $taskLen
        }
    }
}

# Sort by ID for cleaner output
$results = $results | Sort-Object Id

# B5-2079: guard the whole statistics block. Before this guard an empty wave
# indexed into a null array three times (the Median, the p90 and the Mean) and
# still fell through to the "FULLY COMPLIES" branch below, because $null -le 4397
# evaluates true. Measuring nothing is not compliance.
if ($results.Count -eq 0) {
    Write-Output "-- B5-2079: NO ROWS MATCHED THIS WAVE --"
    Write-Output "  Count: 0"
    Write-Output ""
    Write-Output "VERDICT: NOT MEASURED -- 0 rows in this wave, so no budget claim can be made. This is NOT a pass. Exit 2."
    Write-Output "  A green verdict line above a Count of 0 is meaningless: check the count, not just the verdict."
    exit 2
}

# Calculate statistics
$fleetMean = ($results.TaskLen | Measure-Object -Average).Average
$sortedLen = @($results.TaskLen | Sort-Object)
$fleetMedian = $sortedLen[([math]::Floor($sortedLen.Count/2))]
$p90 = $sortedLen[([math]::Floor($sortedLen.Count * 0.9))]
$max = ($results.TaskLen | Measure-Object -Maximum).Maximum

# Budget thresholds from proposal B5-1015/1527
$proposedMean = 764
$proposedMedian = 479
$proposedP90 = 1879
$proposedMax = 4397

# Output
"Id | TaskLen | WithinBudget (<= 1879) | WithinLimit (<= 4397)"
"----|----------|--------------------------|----------------------"
foreach ($r in $results) {
    $withinBudget = $r.TaskLen -le $proposedP90
    $withinLimit = $r.TaskLen -le $proposedMax
    "$($r.Id) | $($r.TaskLen) | $withinBudget | $withinLimit"
}

""
"Summary statistics for Task cells in this wave:"
"  Count: $($results.Count)"
"  Mean: $([math]::Round($fleetMean, 1))"
"  Median: $fleetMedian"
"  p90: $p90"
"  Max: $max"
""
"Proposed budget thresholds (from B5-1471): mean <= $proposedMean, median <= $proposedMedian, p90 <= $proposedP90, max <= $proposedMax"
""

# Verdict
$compliesWithBudget = $results.TaskLen | Where-Object { $_ -le $proposedP90 } | Measure-Object | Select-Object -ExpandProperty Count
$allWithinLimit = $max -le $proposedMax
$meanInRange = $fleetMean -le $proposedMean

if ($allWithinLimit -and $compliesWithBudget -eq $results.Count) {
    "VERDICT: This wave FULLY COMPLIES with the proposed Task-cell budget (measured $($results.Count) rows; exit 0 means measured, read this line for pass/fail)."
} elseif ($allWithinLimit) {
    "VERDICT: This wave COMPLIES with hard limits but exceeds budget median; $compliesWithBudget of $($results.Count) rows within budget ($proposedP90), mean $([math]::Round($fleetMean,1)) vs $proposedMean (measured $($results.Count) rows; exit 0 means measured)."
} else {
    "VERDICT: This wave does NOT comply; max $max exceeds proposed max $proposedMax; $compliesWithBudget of $($results.Count) rows within budget (measured $($results.Count) rows; exit 0 means measured)."
}

exit 0