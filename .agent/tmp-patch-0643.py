import io

path = ".agent/TASK_LEDGER.md"
with io.open(path, "r", encoding="utf-8", newline="") as f:
    content = f.read()

old = "| B5-0643 | OPEN |"
new = "| B5-0643 | DONE |"
assert content.count(old) == 1
content = content.replace(old, new)

lines = content.split("\n")
row_idx = None
for i, line in enumerate(lines):
    if line.startswith("| B5-0643 | DONE |"):
        row_idx = i
        break
assert row_idx is not None

cells = lines[row_idx].split("|")
assert cells[1].strip() == "B5-0643" and cells[2].strip() == "DONE"
assert len(cells) == 8

claim = "Buffy (glm-5.3-flash)"
verified = (
    "2026-09-27 CLOSED by Buffy (glm-5.3-flash) (loop12, claim 06:06Z; gate B5-0635 DONE satisfied; execution only, zero source edits). Full sweep against the 0631+0637+0635 chain, JDK 1.8.0_292: RUN_TESTS=1 -- CONFORMANCE SUITE PASSED 542/542 (actual count; reconciles exactly as 512 pre-chain + 9 ORD + 9 AMT3 + 12 MJR-AI, no silently dropped or double-registered check) plus SMOKE TEST PASSED (446 cards, round 1 in 17531 ms, 29 AI actions, 4/4 AI decisions legal, exit 0). Probes, all exit 0: B5-0350 TIEBREAK SUITE PASSED 26 checks; B5-0351 AI DIFFICULTY CONTRACT VERIFIED 10/10 with the explicit band re-confirmation the row demands -- EASY pass bias rate 0.48 inside the approved 0.20-0.70 band, 0 illegal of 300, non-deterministic (ev0 73 / ev1 83 / pass 144), MEDIUM/HARD deterministic; B5-0382 station victory 6 checks 0 failures; B5-0383 participation gates PASSED; B5-0384 lead-a-fleet 9 checks 0 failures; B5-0419 war-conflict probe PASSED; B5-0443 human-seat PASSED 36 checks (seed-stochastic count 8/37/36 historical, exit 0 every time, recorded as variance per the B5-0608 lesson). Nothing red, nothing BLOCKED, nothing fixed or edited. Report .agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0643.md; lesson filed under .agent/PATTERNS/Buffy (glm-5.3-flash)/."
)

cells[5] = " " + claim + " "
cells[6] = " " + verified + " "
lines[row_idx] = "|".join(cells)

with io.open(path, "w", encoding="utf-8", newline="") as f:
    f.write("\n".join(lines))

with io.open(path, "r", encoding="utf-8", newline="") as f:
    out = f.read()
row = [l for l in out.split("\n") if l.startswith("| B5-0643 |")][0]
import re
ids = re.findall(r"^\|+\s*(B5-[0-9]{4}[a-z]?)\s*\|", out, re.MULTILINE)
dups = sorted(set(x for x in ids if ids.count(x) > 1))
print("row-pipes:", row.count("|"))
print("duplicate-ids:", dups if dups else "NONE (PASS)")
