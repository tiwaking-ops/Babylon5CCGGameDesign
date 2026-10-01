$patternsRoot = "C:\temp\projects\Babylon5CCGGameDesign\.agent\PATTERNS"
$results = @()

Get-ChildItem $patternsRoot -Directory | ForEach-Object {
    $ns = $_.Name
    $files = Get-ChildItem $_.FullName -File -Filter '*.md' | Sort-Object LastWriteTime -Descending
    if ($files.Count -gt 0) {
        $newest = $files[0]
        $filenameDate = ''
        if ($newest.Name -match '^(\d{4}-\d{2}-\d{2})') {
            $filenameDate = $matches[1]
        }
        $results += [pscustomobject]@{
            Namespace = $ns
            File = $newest.Name
            Path = $newest.FullName
            LastWriteUtc = $newest.LastWriteTimeUtc
            FilenameDate = $filenameDate
        }
    } else {
        $results += [pscustomobject]@{
            Namespace = $ns
            File = '(none)'
            Path = ''
            LastWriteUtc = $null
            FilenameDate = ''
        }
    }
}

$results | Sort-Object Namespace | Format-Table -AutoSize
$results | Export-Csv -Path "C:\temp\projects\Babylon5CCGGameDesign\.agent\pattern_scan.csv" -NoTypeInformation