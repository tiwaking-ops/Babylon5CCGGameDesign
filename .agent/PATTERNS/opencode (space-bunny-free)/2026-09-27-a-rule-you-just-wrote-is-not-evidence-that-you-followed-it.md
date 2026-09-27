---
document:
  title: "A rule you just wrote is not evidence that you followed it"
  status: "Advisory pattern record"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A rule you just wrote is not evidence that you followed it

Advisory only, same tier as `investigations/` and the rest of this store. Never canonical.
Cite freely; citing confers no authority.

Sixth instance in this namespace. Companions: the self-certifying gate, the
self-check sharing its subjects, the duplicate ID, the proxy check, the
document/enforcer drift, and the absence-only regression suite.

## The rule

> **Authoring a standard and complying with it are separate events, and only one of
> them produces an artefact.** When the two diverge, the standard is the thing that
> was wrong — or, more often, the standard was right and *unreachable by accident*.

The failure is not ignorance of the rule. It is that the rule existed in prose while
the work happened in filenames, where nobody was looking.

## The worked instance

Amendment A1.1 established the identity rule for this repo: sanitise **only** `:` and
`/`, preserve everything else. The evidence is concrete — `Kilo (kilo-auto/free)` maps
to `Kilo (kilo-auto-free).json`, `solar-pro4:free` to `solar-pro4-free.json`, and no
report file in the repo contains a colon.

Eight reports existed for one `agent_id`, split evenly:

```
2026-09-27-opencode (space-bunny-free)-B5-0621.md        <- canonical
2026-09-27-opencode-(space-bunny-free)-seed-wave-9.md    <- violates
```

Written by the same agent, in the same session that wrote the rule.

The violating form is worse than a cosmetic variant, and the reason matters: it also
converts **spaces**, which no rule authorises. Spaces→hyphens does not yield a second
filename for one agent; it yields what looks like a **second agent** — precisely the
condition A1.1 exists to prevent, where two files assert one identity and
`live_claims` is ambiguous however well-formed either file is. So the violation was not
"wrong format" but "manufactured a phantom agent", produced by the agent the rule was
written about.

## Why writing the rule did not prevent it

The rule was written into a proposal, then into a registry, then into a README. Each
step was a document. The filenames were produced by muscle memory and a date-prefixed
template. **Nothing ever compared the two**, so nothing could fail.

This is the same shape as the document/enforcer drift, one level up: there, the
document and the enforcer disagreed about a schema. Here, the document and the
*practice* disagreed about a naming rule, and the practice is not something a test
runs against.

## The companion, from the same session

A human supplied a confident, complete loop specification. Checking it against the
repo — rather than filing it as received — found two stale statements in it:

* it cited the pattern skim as **step 10**; a prior repair had renumbered it to 11;
* it stopped when "the ledger is empty"; the ledger accumulates and never empties.

Both had been invisible *because* the surrounding text was complete and authoritative
in tone. A confident specification is not a verified one, and **the more complete it
looks, the less likely anyone re-reads it.** That is the whole hazard.

## The check that generalises

After writing a standard, **audit your own recent output against it** before you cite
the standard as satisfied. Concretely, cheap and mechanical:

```powershell
# does any artefact of mine violate the naming rule I just wrote?
Get-ChildItem .agent/REPORTS -File | Where-Object { $_.Name -match 'opencode-\(' }
```

Thirty seconds. It found eight files that four documents and a validator had missed,
because none of them looked at the directory.

The general form: **a rule that is only ever *stated* has no enforcement point, and a
rule with no enforcement point is a wish.** Where you cannot add a check — filenames
made by hand, prose conventions, naming taste — the substitute is a periodic audit
against the rule, owned by whoever wrote it.

**Applies to:** naming conventions, output templates, file-layout rules, any standard
whose compliance is expressed in artefacts rather than in code.

**Read before:** citing a standard you authored as evidence that the codebase complies
with it.
