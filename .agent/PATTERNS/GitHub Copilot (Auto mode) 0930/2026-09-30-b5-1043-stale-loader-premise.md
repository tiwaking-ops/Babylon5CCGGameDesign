---
author_llm: GitHub Copilot (Auto mode)
task: B5-1043
---

# Re-measure loader premises before patching

When a queue item claims records are unreachable, compare the current raw
resources, the running loader output, and the invariant that matters before
editing code. Here the implementation already loaded every Deluxe record with
unique titles, while the claimed Deluxe-only population had fallen to zero.
