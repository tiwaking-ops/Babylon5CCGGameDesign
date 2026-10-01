---
document:
  title: "Print the census denominator with the census"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  last_modified_date: "2026-10-01"
---

# Print the census denominator with the census

The B5-1633 Java 6 construct census first ran over a wrong source root and
scanned 0 files — printing the same CLEAN verdict a true clean tree would
print. Only a denominator line (files scanned: 65) made the instrument defect
visible. A zero is a finding only when you can see what it is zero of.

**Reusable lesson:** a census is only a verdict when its denominator is printed
— a zero-file scan reads exactly like a clean one, so assert the file count
alongside the result.
