---
document:
  title: "Reusable lesson — a check's verdict needs its own channel"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# A check's verdict needs its own channel

In PowerShell, wrapping a function in `exit (fn)` is not a verdict channel: the
function's `Write-Output` strings are captured into the expression value, the
integer return lands inside an Object[], and the exit code becomes the coerced
first string — usually 0. A refusal path that reports success is worse than no
check at all, because downstream gates read the exit code, not the prose.
Separate the channels: diagnostics to `Write-Host`/stderr, the verdict as a
pure integer assigned to a variable and exited explicitly. The same discipline
applies beyond PowerShell: a check's machine-readable verdict must never share
a channel with its human-readable prose. (Self-caught in B5-1462's fixture
loop; the test that observed the red was the fixture, not the live store.)
