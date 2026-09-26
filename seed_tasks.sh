cd /c/temp/projects/Babylon5CCGGameDesign && python3 - << 'PYEOF'
queue_entry = """QUEUE 0511..0514 (2026-09-26, seeding pass, Muse Spark): human ruled on the 0454 brief in the prior pass -- Ruling 1 = A (EASY retune, floor 0.20), Ruling 2 = 2c, Ruling 3 = 3c (mercenary and contingency stay data-gated, no further seeds on either until the human approves a source set; standing directive recorded in DECISIONS). B5-0495 has a live claim by Buffy (glm-5.3-flash) at seed time; B5-0496..0510 are all OPEN unclaimed; no other live claims; only the known residue files on DONE rows. Grounded, not speculated: B5-0508 (EASY retune to floor 0.20) is claimable now per the human ruling; after B5-0508 lands the B5-0351 contract band (widened to 0.20-0.70 in the same claim) needs standalone re-verification to confirm the band still holds with the retuned EASY (shapes 0511); B5-0509 (post-retune balance probe) is gated on B5-0508; B5-0498 (playtest-guide refresh part 13, gated on 0495+0496) will document the smoke-determinism verdict and the 0487 mechanism correction when it lands, and a part-14 refresh should also cover the EASY retune and post-retune probe (shapes 0512); more code tasks have landed since B5-0501's hygiene sweep (B5-0508 will add ai/ + engine/ changes), so another hygiene sweep is warranted (shapes 0513). Serialize: execution-only 0511 gated on 0508 DONE; docs 0512 gated on 0498 + 0508 + 0509 DONE; execution-only 0513 parallel-safe now; git-only 0514 gated on 0510 + 0511 + 0512 + 0513 DONE (claim after 0510 so the checkpoint sequence stays ordered). Nothing seeded on the 0454 brief until the human ruling arrives. Code-task gate unchanged: compile green plus RUN_TESTS=1 green plus Java 6 grep empty on touched dirs. Close-outs touch their ledger row only and preserve table pipes exactly per 00_BOOT step 8 (str_replace with exact anchor only, never write-file a shared governance file to append -- and never write a pipe character in ledger note text per the 0435 standing rule).
"""

b50511 = """| B5-0511 | OPEN | Post-EASY-retune AI contract re-verification (gated: claim ONLY after B5-0508 is DONE; B5-0351 contract band widened to 0.20-0.70 in the same claim that retunes EASY): re-run HeadlessAIDifficultyContractTest standalone CLI to confirm the band 0.20-0.70 still holds with the retuned EASY pass bias; report pass/fail with exact command and counts; execution only, no source edits, report only | harness execution only, no src or resources edits | - | - |
"""

b50512 = """| B5-0512 | OPEN | Playtest-guide refresh part 14 (gated: claim ONLY after B5-0498, B5-0508, and B5-0509 are all DONE): document the EASY retune to floor 0.20 (B5-0508), the post-retune balance re-probe outcome (B5-0509), and the B5-0496 mechanism correction; update suite counts and honesty notes; docs only, no src edits | `docs/` only, no src or resources edits | - | - |
"""

b50513 = """| B5-0513 | OPEN | Build-hygiene re-sweep v2 (B5-0421/B5-0463/B5-0501 precedent): full-tree Java 6 construct grep across b5ccg/src/ plus compile.bat plus compile.sh plus RUN_TESTS=1 green; report exact commands, counts, and any offending file and line; no source edits, report only | execution only, no src or resources edits | - | - |
"""

b50514 = """| B5-0514 | OPEN | Working-tree checkpoint commit (gated: claim ONLY after B5-0510, B5-0511, B5-0512, and B5-0513 are all DONE; overseer-seeded precedent): verify compile.bat green plus RUN_TESTS=1 green first, then commit everything EXCEPT .agent/CLAIMS files and .agent/HEARTBEATS files (transient coordination state, stays uncommitted); do NOT push; report the commit hash plus every file deliberately left out; no src or data edits, commit only | git only, no src or resources edits | - | - |
"""

with open(".agent/TASK_LEDGER.md", "a") as f:
    f.write(queue_entry + b50511 + b50513 + b50512 + b50514)

print("Appended QUEUE 0511..0514 + 4 task rows")
PYEOF