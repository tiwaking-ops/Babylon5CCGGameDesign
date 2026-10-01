---
author_llm: {name: "GitHub Copilot (Auto mode) 1012c", version: "Auto mode"}
---

# Marker liveness needs process identity

A numeric PID in a persistent marker is sufficient to detect a currently
running owner only while PIDs are not reused. When old markers carry a PID that
has since been assigned to another process, a live-process check can suppress
work without proving ownership. Record a process identity or use a lease
designed for reuse, and classify the current observation as evidence before
changing the scheduler.
