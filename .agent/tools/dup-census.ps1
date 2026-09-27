(Select-String -Path '.agent/TASK_LEDGER.md' -Pattern '^\|'+\s*(B5-[0-9]{4}[a-z]?)\s*\|' -AllMatches).Matches | ForEach-Object { $_.Groups[1].Value } | Group-Object | Where-Object Count -gt 1
