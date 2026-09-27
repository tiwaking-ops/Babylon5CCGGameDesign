---
document:
  title: "Heartbeat: terminology provenance and liveness-signal findings"
  status: "Report (observations only, no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Heartbeat: terminology provenance and liveness-signal findings

**Author:** opencode (space-bunny-free), version `space-bunny-free`
**Date:** 2026-09-27
**Status:** Report — observations only, no authority. Per AGENTS.md §3, no authority
derives from this document; it records findings, not truth.
**Companion proposal:** `docs/proposals/heartbeat-schema-proposal.md`

---

## Summary

This report answers two questions. First, where the term "heartbeat" comes from and why
it is technically apt for a fleet of autonomous agents. Second, whether this repository's
implementation matches the concept the term denotes.

**On terminology:** the term is well chosen and the choice is correct. **On
implementation:** the concept is documented in governance, specified in a directory
README, and implemented 32 times over with no common schema — with roughly **10%
adoption of the documented format**. The most reliable liveness signal in the store is the
filesystem mtime, which is the very thing the JSON payload exists to express.

No file was modified to produce this report. The schema proposal is advisory and requires
a human decision to promote.

---

## Part 1 — Provenance of the term

### 1.1 The literal sense

Medicine. A heartbeat is the cheapest available proof of life: a rhythmic signal whose
*absence* carries information precisely because its *presence* costs almost nothing. The
term was chosen for agent liveness for exactly that reason — a periodic, near-free signal
whose silence is diagnostic.

### 1.2 The technical ancestor: distributed-systems failure detection

This is the lineage that matters. In a network, the state "is this peer alive?" is
**genuinely unresolvable** from observation alone. Silence is consistent with at least
four distinct worlds:

| World | Correct action |
|---|---|
| Process crashed | reap the work |
| Process is slow (long GC, long compile) | wait |
| Network partition | wait; the peer is fine |
| Process is idle | nothing to do |

A naive timeout picks one of those and is wrong in the other three. The standard remedy,
formalised in Lamport's failure detectors and implemented in SWIM, gossip protocols,
ZooKeeper and Kafka session timeouts, is a **periodic positive assertion of liveness**:
rather than inferring life from silence, each node announces that it is still there, so
that silence becomes evidence rather than ambiguity.

Two refinements from that literature are directly applicable here and are adopted in the
companion proposal:

- **Liveness and safety must be separated.** Announcing "I am alive" is safe; concluding
  "you are dead" is not. A node must never declare a peer dead on its own initiative.
- **Suspect is a distinct state from dead.** In SWIM this is *suspicion*, escalated only
  after corroboration. This repo has no equivalent state today, which is why
  `UNKNOWN` is proposed as a first-class verdict.

### 1.3 Databases and health checks

- **MySQL `innodb_heartbeat`** — a table with a single row, bumped periodically; the age
  of that row is the instance's liveness. Structurally identical to a heartbeat file.
- **Microsoft SQL Server** — internal heartbeat tables per subsystem.
- **Kubernetes `livenessProbe`** and load-balancer health checks — the same positive
  assertion, wrapped in a controller that acts on it.
- **TCP keepalives** — the oldest and least reliable member of the family, and the one
  most often misapplied.

### 1.4 Why the term is correct for an agent fleet

An agent fleet has the same ambiguity as a network, plus one complication: a claim file
with a 30-minute TTL cannot distinguish "agent died" from "agent is running a 19-second
compile across 40 seeds", because both look like a stale file.

The heartbeat is the third signal that breaks the tie. Hence the rule this repo's own
history arrived at independently:

> A task is **live** when the **newest** of its claim file, its owner's heartbeat, and its
> report mtime falls inside the TTL. (B5-0597)

This session that rule prevented real damage: claims B5-0572, B5-0573 and B5-0574 read
63–85 minutes against a 30-minute TTL and looked reap-eligible, but the owning agent's
heartbeat was minutes fresh and it was actively writing reports. Nothing was reaped.

---

## Part 2 — Findings on implementation

### 2.1 Method

Enumerated all 32 entries in `.agent/HEARTBEATS/`, then for each determined: container
format, timestamp field name, whether a timestamp is parseable, and file mtime age.
Cross-checked against the format specified in `.agent/HEARTBEATS/README.md` and the
liveness requirements in
`docs/reports/multi-agent-documentation-workflow-system-architecture-2026-09-26.md`
(§5, §5.5, and the `heartbeat_interval_minutes: 5` setting at line 401).

Read-only throughout. No heartbeat file was created, modified or deleted in producing
this report, other than this agent's own file per the README's instruction that an agent
maintain its own.

### 2.2 The documented format has ~10% adoption

`.agent/HEARTBEATS/README.md` specifies:

```json
{"agent_id": "hermes-01", "utc": "2026-09-21T12:05:00Z", "current_task": "B5-0001", "javac": "1.8.0_292"}
```

The field is **`utc`**. Survey result: **3 of 31 heartbeat files** use it. The other 28
invented their own field name.

A governance specification with 10% compliance is arguably worse than none, because it
creates a documented contract that readers reasonably assume is honoured, and that
validation cannot check without a schema.

### 2.3 Four incompatible timestamp field names

| Field name | Source | Files |
|---|---|---|
| `utc` | `README.md` (the spec) | 3 |
| `updated_utc` | ad hoc | 9 |
| `heartbeat_utc` | ad hoc | 8 |
| `last_heartbeat_utc` | ad hoc | 1 |
| *none parseable* | — | **19** |

**19 of 32 files carry no parseable timestamp at all.** A reader looking for the
documented `utc` field finds it in 3 files; a reader looking for any timestamp field
finds nothing in 19.

### 2.4 Container-format drift

| Anomaly | Files | Consequence |
|---|---|---|
| YAML frontmatter, not JSON | `agent-on-deck.json`, `goose-b5ccg-agent.json` | `ConvertFrom-Json` **throws** |
| No file extension | `solar-pro4` | glob-based readers miss it |
| Wrong extension | `kiro-pi.timestamp` | not a `.json` at all |
| Not a heartbeat | `README.md` (fine — it is the spec) | — |

**Observed consequence, first-hand.** A survey in this session that iterated
`.agent/HEARTBEATS/*.json` and parsed each file **threw on the first YAML file and
returned nothing**. A second attempt that ignored contents entirely returned 0 rows and
nearly reported "0 open tasks" — a false answer about the state of the entire project.
One malformed file is enough to make a whole census silently wrong, because the error
aborts the loop rather than skipping one entry.

### 2.5 Stale heartbeats are indistinguishable from live ones

| Age | Count | Reading |
|---|---|---|
| < 1 h | 4 | plausibly live |
| 1–3 h | 2 | stale |
| > 1 day | 7 | long dead |
| > 5 days | 1 (`big-pickle.json`, 8023 min) | dead ~6 days |

There is no `state` field in the spec and no consistent one in practice, so a reader
cannot distinguish "idle" from "dead" from "abandoned" without consulting mtime. A
heartbeat that stopped beating six days ago is not a heartbeat; it is a file.

### 2.6 `agent_id` fragmentation breaks the join

| Agent | Distinct spellings on disk |
|---|---|
| solar-pro4 | `solar-pro4`, `solar-pro4:free`, `solar-pro4⟨U+2028⟩free` |
| Buffy | `Buffy (glm-5.3-flash)`, `Buffy (unknown)`, `Buffy-(glm-5.3-flash)`, `buffy-unknown-loop2` |
| kilo | `kilo`, `kilo-auto`, `Kilo (kilo-auto-free)` |
| GPT-6 | `GPT-6 Codex (GPT-6)`, `GPT-6-Codex`, `codex-gpt6-01` |
| opencode | `opencode (me-so-poor)`, `me-so-poor` |
| Kiro | `kiro-pi`, `kiro-pi.timestamp` |

An exact-string join on `agent_id` **cannot match across these spellings**, so a liveness
scan concludes "no heartbeat found" for an agent that is alive. This is B5-0584's
subject, and it is the same failure mode as the ledger's pipe-fragmentation problem: a
*structural* defect in the data model that a naive reader cannot see past.

One of those spellings, `solar-pro4⟨U+2028⟩free`, contains U+2028 (LINE SEPARATOR) — an
invisible character inside a **filename**. It renders as a space in most tools and breaks
naive string matching. Note that this agent's own identity was chosen as a deliberate new
spelling (`opencode (space-bunny-free)`) rather than reusing the prior lineage's
nickname, because claiming another model's provenance would be fabrication under
AGENTS.md §1 — which means this report adds one row to the very census B5-0584 is trying
to complete. That is disclosed rather than hidden.

### 2.7 The three documented false reads

All three are on record; none was detected by a heartbeat reader at the time.

| # | Failure | Record | Severity |
|---|---|---|---|
| 1 | Reaper keyed on claim mtime deletes claims **under a live worker** (claims 0572/0573/0574 read 63–85 min vs a 30-min TTL, owner actively writing reports) | B5-0597 | destroys work; invites duplicate delivery |
| 2 | Reader matched task IDs inside a heartbeat's **narrative** `last_completed` prose and concluded 4 claims were held, when `live_claims` was an empty list | B5-0609 | invents state |
| 3 | Lookup matching no heartbeat file returned `-1`; `-1` compared as **younger** than the TTL, so 4 claims were reported **LIVE** with zero verification | B5-0597 | worst class |

Failure 3 is the most instructive. A missing signal was not reported as missing — it was
converted into a value that satisfied the freshness test. **The tool manufactured a
positive liveness claim out of an absent signal.** This is the exact inversion of the
concept: a heartbeat's purpose is to make absence *visible*, and a `-1` sentinel makes
absence invisible while asserting presence.

The same inversion appears in the B5-0613 self-check, where a verification shared its
predicate with the code it verified and therefore could not fail. Two independent
instances in one session of the same structural error: **a signal that cannot represent
absence will always resolve absence into a confident answer.**

### 2.8 Filesystem mtime outperforms the payload

The most reliable liveness signal available during this survey was
`LastWriteTimeUtc` — not the JSON contents, because the contents were malformed or
timestamp-less in 19 of 32 files, while the OS maintains mtime without the writer's
cooperation.

This is worth stating plainly because it is mildly humbling: the convention exists to
replace "trust the filesystem" with "trust the protocol", and in this store the protocol
is currently *less* reliable than what it replaced.

---

## Part 3 — Assessment

**The terminology is sound and the concept is correctly chosen.** "Heartbeat" names
precisely the right primitive: a cheap, periodic, positive assertion of liveness whose
absence is diagnostic. An agent fleet needs it for the same reasons a network does, and
this repo's own B5-0597 arrived at the correct three-signal rule independently.

**The implementation does not yet match the concept.** The gap is not conceptual — every
agent in this repo understood what a heartbeat was for. The gap is that the store has no
enforced schema, so a governance-level specification is honoured by roughly one file in
ten and cannot be validated.

Three structural defects recur across this repository and are worth naming together,
because they are the same mistake in different clothes:

1. **Ledger rows** — a documented canonical 7-pipe form honoured by most rows; the
   exceptions were invisible to the tool that read them, so a query tool reported the
   queue drained while 7 claimable rows sat on disk. (B5-0613, and the status residual
   corrected in `docs/DECISIONS.md`.)
2. **Heartbeat files** — a documented canonical JSON form honoured by ~10%; the
   exceptions were unparseable, so a survey threw or silently returned nothing.
3. **Agent identities** — no canonical spelling, so joins on the obvious key fail
   silently. (B5-0584.)

Each is a case of *specification without validation*. In a system where agents cannot
negotiate with each other, a documented convention that is not machine-checked is
indistinguishable from no convention — while looking, to every reader, like a contract
that holds.

**Not asserted here:** any intent behind the current state, any ruling on B5-0584's
census, or any change to `00_BOOT.md`, which is governance. The parked mercenary and
contingency backlog remains governed by Ruling 2c/3c and is untouched by this report.

---

## Recommendations

Ordered by leverage. Full detail in `docs/proposals/heartbeat-schema-proposal.md`.

1. **Make one field binding.** Adopt the README's existing `utc` as *the* timestamp
   field; keep legacy keys for one cycle. Removes four spellings without inventing a
   fifth.
2. **Add an `UNKNOWN` verdict.** An absent or unparseable signal must report `UNKNOWN`
   with a reason. Never `-1`, never `0`, never a silent default. Directly closes failure
   2.4 and §2.7 failure 3.
3. **Require `live_claims` always present**, `[]` when empty — a positive assertion of
   holding nothing, distinguishable from omission.
4. **Mark `notes` as prose, permanently.** Any value a machine must read gets its own
   field. Closes §2.7 failure 2.
5. **Ship the read-only validator** and make a parse failure on *any* file a loud,
   non-zero result rather than a truncated loop.
6. **Fix `agent_id` spelling** — highest risk, needs human approval, must not happen in
   the same pass as any field rename. Defer to B5-0584.

---

## Reusable lesson

A heartbeat is a **positive assertion of liveness**, so the schema's real job is to make
every state expressible — including *"I don't know"*. A liveness system with no way to
represent absence will always resolve absence into a confident answer, and a confident
wrong answer about whether work is live costs more than no answer: it destroys work and
manufactures duplicate delivery. Concretely, in one session this repository produced two
independent instances of the same structural error — a `-1` sentinel that made a missing
heartbeat look *live*, and a self-check that shared its predicate with the code it
verified and so could not fail. Both were green. Neither was a bug in the reasoning; both
were missing `UNKNOWN`.
