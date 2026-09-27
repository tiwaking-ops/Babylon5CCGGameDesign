---
document:
  title: "A stub written in the calling language will confirm what the real call does not do"
  status: "Advisory pattern record"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A stub written in the calling language will confirm what the real call does not do

Advisory only, same tier as `investigations/` and the rest of this store. Never canonical.
Cite freely; citing confers no authority.

Eighth instance in this namespace, and the first about **the test being wrong rather
than the code**. Companions: the self-certifying gate, the self-check sharing its
subjects, the duplicate ID, the proxy check, the document/enforcer drift, the
absence-only suite, the rule you just wrote, the procedure nothing launches.

## The rule

> Reproduce a boundary bug with a probe that **crosses the same boundary**.
> A stub that runs on the caller's side of the boundary cannot fail the way the real
> call fails — and will report green while the real thing is broken.

Every layer that can transform your data is a place a test can be quietly measuring
the wrong thing.

## The worked instance

A queue runner invoked an agent CLI like this:

```powershell
& $AgentCli @argList          # $argList = @('-z', $prompt)
```

and it failed in production:

```
hermes: 'lesson line filed as a NEW file under …' is not a `hermes` command.
exit 2
```

The prompt had been split, and the tail was read as a command name.

**Verification attempt 1 — a `.ps1` stub.** Same language as the caller. It reported
`ARGC=1`, one clean argument, no problem. Convincing, and **worthless**: a PowerShell
script receives .NET objects, so it never goes through the Windows command-line
marshaller. It cannot reproduce the bug, so its green told me nothing about the code
path that actually runs.

The tell was noticing that the stub and the caller were the same kind of thing. When
your harness and your system share an implementation, you have tested the harness.

**Verification attempt 2 — a native `python.exe` argv dumper.** Different
implementation, same Windows marshalling path as `hermes.exe`. It reported:

```
fixed prompt   -> ARGC=1   (1007 chars, newlines preserved)
quoted prompt  -> ARGC=2   (fragments of 18 and 38 chars)
```

That is the real path, and it reproduced the failure and the fix.

## Mechanism worth keeping

Windows PowerShell 5.1 does **not** escape embedded double quotes when passing an
argument to a native executable. Quotes act as delimiters, so one string arrives as
several `argv` entries. CLIs that take a **bare positional prompt** are unaffected,
which is exactly why this survived: the runner had presumably only ever been pointed
at one of those. **A defect that only reproduces against some of your consumers will
hide until you try the other kind.**

And the first hypothesis was wrong in a way that mattered: newlines were suspected
first, and a two-line prompt exited 0. Had the "fix" been the obvious one — collapse
the prompt to a single line — the quotes would still have split it.

## The generalisation

When your test passes and production fails, ask **which side of the boundary the test
runs on.** Concretely:

| real system | probe must be |
|---|---|
| shelling out to a binary | a different binary, or a real one |
| writing a file another process reads | written and read by different code |
| shelling out with tricky quoting | a probe that prints `argv`, not a shell script |
| an HTTP client | a request that crosses the wire, not a mocked function |

And a corollary about probes generally: **a probe that runs against a live tool must
be incapable of causing a side effect.** The isolation probe here told a live agent
with file tools to write a file, into another agent's namespace, and it did — because
tools do what they are told. The discipline belongs to whoever writes the probe, not to
the tool.

**Applies to:** any test harness, mock, stub, dry-run or smoke test that stands in for
a boundary — process spawning, IPC, file formats, wire protocols, anything with a
marshaller between you and the thing that fails.

**Read before:** trusting a green stub, or writing a probe against a live system.
