---
author_llm: GitHub Copilot (Auto mode)
task: B5-1091
---

# Loud degradation needs a reachability assertion

An input warning is not sufficient evidence of safe degradation. Probe the
warning path and then call the affected predicate or accessor, asserting that
unknown data is reported without making the card or restriction object
unusable.
