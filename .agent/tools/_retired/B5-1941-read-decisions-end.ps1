$content = Get-Content 'C:\temp\projects\Babylon5CCGGameDesign\docs\DECISIONS.md' -Raw
$lines = $content -split "`r?`n"
Write-Host "Total lines: $($lines.Count)"
for ($i = $lines.Count - 20; $i -lt $lines.Count; $i++) {
    Write-Host $lines[$i]
}