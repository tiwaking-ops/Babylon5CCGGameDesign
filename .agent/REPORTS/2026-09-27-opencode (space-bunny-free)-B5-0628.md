---
document:
  title: "B5-0628 — the runner could not pass a prompt containing quotes to any native CLI"
  status: "Report (no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0628 — PowerShell 5.1 will not pass a quoted prompt to a native CLI

Origin: **the human's own failed run**, not a seeded row. Their terminal showed:

```
hermes: 'lesson line filed as a NEW file under
.agent/PATTERNS/<agent-id>/, delete your claim file, ... state section is stale.'
       is not a `hermes` command.
[1] Agent exit code: 2.
```

The prompt had been **split**, and the tail fragment was read as a command name.

## Reading the error before theorising

The fragment hermes quoted begins immediately after the phrase `"Reusable lesson"` in
the task template. That located the cause before any hypothesis was formed — and the
first hypothesis would have been wrong.

## Four controlled cases, run against hermes itself

| case | prompt | result |
|---|---|---|
| A | two lines, separated by newlines | exit 0, understood → **newlines are safe** |
| B | same, newlines collapsed | exit 0 |
| C | contains `"Reusable lesson"` | **exit 2, the human's exact error** |
| D | same prompt, quotes removed | exit 0, **executed the instruction** |

Newlines were my first suspicion and the reproduction killed it. Had I "fixed" it by
collapsing the prompt to one line — the obvious reflex — the quotes would still have
split it and the runner would still have been broken.

## Mechanism

Windows PowerShell 5.1 does **not** escape embedded double quotes when handing an
argument to a native executable. The quotes act as argument delimiters, so:

```
hermes -z  Write one "Reusable lesson" line and stop.
                 ^-- ARGC=2: "Write one " and "Reusable lesson" line and stop."
```

The first fragment is consumed as the prompt; the rest become positional arguments;
a subcommand-style CLI reports the last as an invalid command. Agent CLIs that take a
bare positional prompt (`claude "…"`) are unaffected, which is why this survived
unnoticed — the runner had presumably only ever been pointed at one of those.

## Proof through the same path hermes uses

A `.ps1` stub was tried first and **deliberately not trusted**: a PowerShell stub
receives .NET arguments and never exercises native marshalling, so `ARGC=1` from it
would have been a false proof. Recognising that, a native `python.exe` argv dumper was
used instead — it receives arguments through the identical Windows path.

End-to-end, running the **real runner** against that dumper:

```
ARGC=1                    <- prompt arrived as ONE native argument
ARG[0]_LEN=1007
CONTAINS_DOUBLEQUOTE=False
NEWLINE_COUNT=12          <- newlines deliberately preserved
HAS_TASKID=True
```

Control, same path, quotes retained: `ARGC=2`, fragments of 18 and 38 characters.

## The fix, and why it is that small

```powershell
$safePrompt = $prompt -replace '"', "'"
```

One line, at the single invocation site, with a comment recording the reproduction so
the next reader need not rediscover it. Single quotes read identically to the model.
The prompt's multi-line structure is **preserved** — newlines were tested and are
harmless, so flattening the prompt would have been a larger change than the evidence
supports.

## Side effect I caused, and removed

Isolating the trigger meant sending hermes a live probe prompt. I wrote one that said
*"Write one reusable lesson line"* — and **hermes did exactly that**, creating:

```
.agent/PATTERNS/solar-pro4<U+2028>free/lesson-compile-gate-grep.md
```

That is **solar-pro4's namespace**, which this agent may read and must never write.
The file was confirmed as this session's own creation by its `04:24:28Z` creation time
against a `02:34Z` session start, its contents were read to confirm what it was, and
it was deleted. The eight pre-existing files in that namespace were untouched.

**The mistake worth recording:** a reproduction run against a live tool must use a
prompt that *cannot* cause a side effect. I sent a writing instruction to an agent
with file tools, in a directory belonging to someone else, and it worked perfectly —
which was the problem. Tools do what they are told; the discipline belongs to whoever
writes the probe.

## Still open, not fixed

`-AgentArgs` appends its value as a **single** array element, so multi-flag strings
like `'-z --yolo'` arrive as one malformed argument. Unchanged by this task: fixing it
means either splitting on whitespace (which would break paths containing spaces) or
adding a wrapper script. Neither is a one-liner, so it is reported rather than
absorbed.

**Reusable lesson:** when a native call fails in a way that looks like the *tool*
misunderstanding your input, suspect the shell's argument marshalling before you
suspect the tool — and reproduce with a dumper that receives arguments through the
same path, because a stub written in the calling language will happily confirm
something the real call does not do.
