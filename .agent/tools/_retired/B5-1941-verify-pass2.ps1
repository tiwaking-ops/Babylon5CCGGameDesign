$retired = Get-ChildItem 'C:\temp\projects\Babylon5CCGGameDesign\.agent\HEARTBEATS\_retired\*.json'
$live = Get-ChildItem 'C:\temp\projects\Babylon5CCGGameDesign\.agent\HEARTBEATS\*.json' | Where-Object {$_.Name -notmatch '^_' -and $_.Name -ne 'README.md' -and $_.Name -ne '_registry.json'}

Write-Host "Retired count: $($retired.Count)"
Write-Host "Live count: $($live.Count)"
Write-Host "Registry exists: $(Test-Path 'C:\temp\projects\Babylon5CCGGameDesign\.agent\HEARTBEATS\_registry.json')"

$tombstoneLive = Test-Path 'C:\temp\projects\Babylon5CCGGameDesign\.agent\HEARTBEATS\Cline (space-bunny) b5-0941.json'
$tombstoneRetired = Test-Path 'C:\temp\projects\Babylon5CCGGameDesign\.agent\HEARTBEATS\_retired\Cline (space-bunny) b5-0941.json'
Write-Host "Tombstone in live: $tombstoneLive"
Write-Host "Tombstone in retired: $tombstoneRetired"

# Verify SHA-256 matches for all retired files (compare with original if we can)
# For now just confirm all retired files are readable
$allReadable = $true
foreach ($f in $retired) {
    try {
        $content = Get-Content $f.FullName -Raw -Encoding UTF8
        $json = $content | ConvertFrom-Json -ErrorAction Stop
    } catch {
        Write-Warning "Failed to read $($f.Name): $_"
        $allReadable = $false
    }
}
Write-Host "All retired files readable: $allReadable"

# Count by agent prefix
$retiredNames = $retired | Select-Object -ExpandProperty Name
$liveNames = $live | Select-Object -ExpandProperty Name
Write-Host "`nRetired files:"
$retiredNames | Sort-Object | ForEach-Object { Write-Host "  $_" }