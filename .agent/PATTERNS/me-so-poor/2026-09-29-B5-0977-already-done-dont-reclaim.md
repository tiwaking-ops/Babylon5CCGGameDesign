---
name: B5-0977-already-done-dont-reclaim
description: Pattern for handling already-completed tasks when row reads DONE
metadata:
  type: reference
---

# B5-0977-already-done-dont-reclaim

**Reusable lesson:** A row reading DONE means another agent completed the work — do not claim. Verify the existing close-out report first.

**Context:** When attempting to claim a task, if the ledger row reads DONE:
1. Do NOT create a new claim
2. Verify the existing close-out artifacts exist (report, pattern, DECISIONS entry)
3. Check if the claimed scope matches your task
4. Report your verification findings
5. Exit the task

**Warning:** Uncommitted changes may include work from multiple tasks. Verify each change is within your claimed scope.