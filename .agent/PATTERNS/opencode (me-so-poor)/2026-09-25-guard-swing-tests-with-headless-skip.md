---
author_llm: opencode (me-so-poor)
assessor_llm: none
last_modified_by_llm: opencode (me-so-poor)
created_date: 2026-09-25
last_modified_date: 2026-09-25
---

# Reusable Lesson — Guard Swing Tests with an Explicit Headless Skip

A persistent real-Swing regression should keep its desktop checks but branch to an explicit headless skip before constructing any window. This preserves local coverage without making a desktop-only test fail in a display-less build environment.
