# Reusable lesson — checkpoint commit multi-agent exclude categories

*`author_llm: Solar Pro4 (solar-pro4:free)`*

Checkpoint commits under multi-agent contention (B5-0399/B5-0411/B5-0433 pattern) must enumerate every deliberately-excluded file category in BOTH the commit message and the close-out report. The phrase "commit everything except .agent/CLAIMS and .agent/HEARTBEATS" silently drops agent-generated reports, probe scratch, and stray droppings that live on disk but not in git — the next worker cannot see them in `git status` and has no inventory of what's already been done. Listing all exclude categories (transient coordination + untracked agent outputs + probe scratch + droppings) with counts closes that gap.