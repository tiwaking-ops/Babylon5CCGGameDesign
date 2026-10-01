---
document:
  title: "Parity is one decision surface, not two copies"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Parity is one decision surface, not two copies

**Task:** B5-0967 (compile.bat / compile.sh test-branch parity, 2026-09-28).

**The trap.** Two platform variants of one build gate, one with a test branch and one
without — the obvious "fix" is to copy the test branch into the thinner script so both
platforms gate equally. That doubles the surface on which the gate list can rot: the
first edit to either copy diverges them, and the divergence is invisible until the two
platforms disagree about whether the same tree is green. Divergent twins also forge
contradictory evidence — a red gate on one platform and a green gate on the other for
the identical bytes.

**The delegation shape.** Make the thinner twin *point* at the thicker one: a comment
at exactly the place a reader notices the asymmetry, naming the single command that
runs the full gate, the one file where the selection is decided, and the fallback for
environments without the POSIX layer. The gate list stays decided in exactly one file;
the asymmetry becomes documented intent instead of a silent half-gate; and the
decision survives edits because there is nothing to edit twice.

**Three checks that made this decision safe rather than merely tidy.**
1. **Re-measure the premise at claim time.** The gate tier being "decided about" had
   landed (B5-0956) while the row sat OPEN — deciding against the seed-time snapshot
   would have produced a stale answer on a fresh script.
2. **Prove the delegation command on the real platform.** `RUN_TESTS=1 sh compile.sh`
   exit 0 end-to-end through the same shell every agent actually uses — a pointer to
   an untested command is a hope, not a gate.
3. **Verify the touched script still runs.** The comment-only edit was re-measured
   (`cmd //c compile.bat` exit 0), because "comment-only" is an assumption until the
   file executes.

**And when the asymmetry is really an invocation gap, say so.** The row framed the
question as platforms ("the platform every agent actually builds on"), but the
recorded history shows every agent builds through the same POSIX-ish shell — the gap
was between *invocations* of one platform, which changes the remedy from "port the
gate" to "document the command".
