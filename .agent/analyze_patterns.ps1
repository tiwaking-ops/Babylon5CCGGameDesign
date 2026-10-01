$patternsRoot = "C:\temp\projects\Babylon5CCGGameDesign\.agent\PATTERNS"
$b51455Time = [DateTime]::Parse("2026-10-01T02:52:00Z")

# Stale namespaces from B5-1455
$b51455Stale = @(
    "Antigravity",
    "Buffy (unknown)",
    "Buffy-(glm-5.3-flash)",
    "Cline (space-bunny) b5-0941",
    "muse-spark",
    "opencode (me-so-poor)",
    "opencode (space-bunny-free)"
)

$results = @()

Get-ChildItem $patternsRoot -Directory | ForEach-Object {
    $ns = $_.Name
    $files = Get-ChildItem $_.FullName -File -Filter '*.md' | Sort-Object LastWriteTime -Descending
    if ($files.Count -gt 0) {
        $newest = $files[0]
        $filenameDate = $null
        if ($newest.Name -match '^(\d{4}-\d{2}-\d{2})') {
            $filenameDate = [DateTime]::ParseExact($matches[1], "yyyy-MM-dd", $null)
        }
        
        # Selection rule: newest filename date, then file modification time
        $selectionDate = if ($filenameDate) { $filenameDate } else { $newest.LastWriteTimeUtc }
        
        $isNewer = $selectionDate -gt $b51455Time
        $wasStale = $b51455Stale -contains $ns
        $nowStale = $selectionDate -lt [DateTime]::Parse("2026-09-28")
        
        $results += [pscustomobject]@{
            Namespace = $ns
            NewestFile = $newest.Name
            FilenameDate = if ($filenameDate) { $filenameDate.ToString("yyyy-MM-dd") } else { "(none)" }
            SelectionDate = $selectionDate.ToString("yyyy-MM-ddTHH:mm:ssZ")
            NewerThanB51455 = $isNewer
            WasStaleInB51455 = $wasStale
            NowStale = $nowStale
            Status = if ($wasStale -and -not $nowStale) { "NEWLY ACTIVE" } elseif ($wasStale -and $nowStale) { "STILL STALE" } elseif ($isNewer) { "NEW RECORD" } else { "UNCHANGED" }
        }
    } else {
        $results += [pscustomobject]@{
            Namespace = $ns
            NewestFile = "(none)"
            FilenameDate = "(none)"
            SelectionDate = "(none)"
            NewerThanB51455 = $false
            WasStaleInB51455 = $false
            NowStale = $false
            Status = "EMPTY"
        }
    }
}

# Also check for namespaces that existed in B5-1455 but not in current scan
$currentNamespaces = $results | Select-Object -ExpandProperty Namespace
$b51455Namespaces = @(
    "Antigravity",
    "Buffy (glm-5.3-flash)",
    "Buffy (glm-5.3-flash) 10",
    "Buffy (glm-5.3-flash) 11",
    "Buffy (glm-5.3-flash) 12",
    "Buffy (glm-5.3-flash) 13",
    "Buffy (glm-5.3-flash) 14",
    "Buffy (glm-5.3-flash) 15",
    "Buffy (glm-5.3-flash) 2",
    "Buffy (glm-5.3-flash) 3",
    "Buffy (glm-5.3-flash) 4",
    "Buffy (glm-5.3-flash) 5",
    "Buffy (glm-5.3-flash) 6",
    "Buffy (glm-5.3-flash) 7",
    "Buffy (glm-5.3-flash) 8",
    "Buffy (openai-gpt-6-luna) 1",
    "Buffy (unknown)",
    "Buffy-(glm-5.3-flash)",
    "Cline (space-bunny) b5-0737",
    "Cline (space-bunny) b5-0807",
    "Cline (space-bunny) b5-0811",
    "Cline (space-bunny) b5-0831",
    "Cline (space-bunny) b5-0833",
    "Cline (space-bunny) b5-0835",
    "Cline (space-bunny) b5-0837",
    "Cline (space-bunny) b5-0839",
    "Cline (space-bunny) b5-0841",
    "Cline (space-bunny) b5-0919",
    "Cline (space-bunny) b5-0923",
    "Cline (space-bunny) b5-0925",
    "Cline (space-bunny) b5-0933",
    "Cline (space-bunny) b5-0941",
    "GPT-6 Codex (GPT-6)",
    "Grok (4.7)",
    "kilo (kilo-auto-free)",
    "kilo (nvidia-nemotron-3-ultra-550b-a55b-free)",
    "kilo (nvidia-nemotron-3-ultra-550b-a55b-free) B5-1031",
    "me-so-poor",
    "me-so-poor-opencode-02",
    "Muse Spark (muse-spark-1.3) seed-09",
    "muse-spark",
    "muse-spark (muse-spark-1.3) seed-10",
    "muse-spark-b5-1002",
    "muse-spark-opencode-02",
    "opencode (big-pickle)",
    "opencode (big-pickle) loop1",
    "opencode (big-pickle) rel1008",
    "opencode (big-pickle) vb1067",
    "opencode (big-pickle-free)",
    "opencode (big-pickle-free) bp3",
    "opencode (big-pickle-free) bp4",
    "opencode (me-so-poor)",
    "opencode (me-so-poor) 2",
    "opencode (muse-spark-1.3) approve-01",
    "opencode (muse-spark-1.3) seed-07",
    "opencode (space-bunny-free)",
    "opencode (space-bunny-free) 2",
    "opencode (space-bunny-free) 3",
    "opencode (space-bunny-free) 4",
    "opencode (space-bunny-free) 5",
    "opencode (space-bunny-free) 6",
    "opencode (space-bunny-free) 7",
    "opencode-me-so-poor-loop2",
    "solar-pro4",
    "solar-pro4-free",
    "solar-pro4-free-0978",
    "solar-pro4-free-b51088blocked",
    "solar-pro4?free"
)

$missingNamespaces = $b51455Namespaces | Where-Object { $_ -notin $currentNamespaces }
if ($missingNamespaces.Count -gt 0) {
    foreach ($ns in $missingNamespaces) {
        $results += [pscustomobject]@{
            Namespace = $ns
            NewestFile = "(namespace missing)"
            FilenameDate = "(none)"
            SelectionDate = "(none)"
            NewerThanB51455 = $false
            WasStaleInB51455 = $b51455Stale -contains $ns
            NowStale = $false
            Status = "NAMESPACE MISSING"
        }
    }
}

$results | Sort-Object Namespace | Format-Table -AutoSize
$results | Export-Csv -Path "C:\temp\projects\Babylon5CCGGameDesign\.agent\pattern_analysis.csv" -NoTypeInformation