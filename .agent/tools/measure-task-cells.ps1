# Measure Task-cell weights for rows after B5-1471 proposal date (2026-09-30)
param(
    [string]$LedgerPath = ".agent/TASK_LEDGER.md"
)

# Row regex to extract id and Task cell (3rd pipe-delimited field after id)
# Format: | B5-XXXX | STATUS | TASK | SCOPE | VERIFIED | OWNER | DATE
$rowRegex = [regex]'^\|+\s*(B5-\d{4}[a-z]?)\s*\|\s*([^|]*)\s*\|'

$lines = Get-Content $LedgerPath -Raw -Encoding UTF8 -Split "`n"

$results = @()
foreach ($line in $lines) {
    $m = $rowRegex.Match($line)
    if (-not $m.Success) { continue }

    $id = $m.Groups[1].Value.Trim()
    $taskCell = $m.Groups[2].Value.Trim()
    $taskLen = $taskCell.Length

    # Check if row is after B5-1471 (proposal date 2026-09-30)
    # B5-1471 is the proposal, so IDs > 1471 (1473, 1475, 1477, 1479, 1523, 1525, 1527, etc.)
    if ([int]$id.Substring(3) -gt 1471) {
        $results += [pscustomobject]@{
            Id = $id
            TaskLen = $taskLen
        }
    }
}

# Sort by ID for cleaner output
$results = $results | Sort-Object Id

# Calculate statistics
$fleetMean = ($results.TaskLen | Measure-Object -Average).Average
$fleetMedian = ($results.TaskLen | Sort-Object)[[math]::Floor($results.Count/2)]
$p90 = ($results.TaskLen | Sort-Object)[math]::Floor($results.Count * 0.9)]
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
    "VERDICT: This wave FULLY COMPLIES with the proposed Task-cell budget."
} elseif ($allWithinLimit) {
    "VERDICT: This wave COMPLIES with hard limits but exceeds budget median; $compliesWithBudget of $($results.Count) rows within budget ($proposedP90), mean $([math]::Round($fleetMean,1)) vs $proposedMean."
} else {
    "VERDICT: This wave does NOT comply; max $max exceeds proposed max $proposedP90; $compliesWithBudget of $($results.Count) rows within budget."
}