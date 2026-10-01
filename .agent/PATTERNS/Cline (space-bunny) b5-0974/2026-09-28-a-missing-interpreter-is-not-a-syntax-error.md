---
document:
  title: "A missing interpreter is not a syntax error"
  status: "Advisory pattern (no authority; .agent/PATTERNS is never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0974", version: "space-bunny"}
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0974", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A missing interpreter is not a syntax error

Two ways a "the check failed" signal is really the *tool* failing, not the
*thing under test*. Both were hit in one pass on B5-0974.

**1. The interpreter was absent, and the tool's exit code said nothing about
the file.** A bare `bash -n b5ccg/compile.sh` returned **exit 1** on a Windows
host with WSL not installed, with stderr `execvpe(/bin/bash) failed: No such
file or directory`. The script was flawless. Calling Git Bash by full path
(`C:\Program Files\Git\bin\bash.exe -n <file>`) returned **exit 0** on the very
same file. The discriminator is in the stderr, not the exit code: an
interpreter-load failure names a missing *executable*, whereas a syntax
failure names a *line* and a *token*. Never file a defect report off a bare
exit code when the tool may not have loaded at all.

**2. The shell being questioned was not the shell the repo runs.** The same
pass, `core.autocrlf=true` with no `.gitattributes` made Git print
`LF will be replaced by CRLF` on a `.sh` file, which reads as a landmine. But
Git Bash executes a CRLF file with a `\r` on the shebang without complaint
(measured: `CRLF_EXIT=0`, 63 sources compiled). The repo's own header comment
warns that CRLF "broke stricter shells" — `dash`, some `sh` — which is a real
constraint, just not the one Git Bash imposes. **Synthesise the bad case and
run it against the interpreter you actually use**; a warning is a prediction
about a future checkout, not a measurement of today's bytes.

The generalisable form, and the same failure the loop doc records as class 2
("absence of an error is not presence of a value"): a checker that did not run
produces an exit code indistinguishable from a checker that ran and failed.
Disambiguate by making the tool say which one happened — here, by reading its
stderr and by naming the interpreter in the invocation.

Second-order lesson worth keeping: when a verification run outlasts the
command timeout, do **not** chase the exit code through an inline shell
one-liner. PowerShell consumes `$?` before bash receives it and the run then
looks hung forever. Have the bash script write `echo "EXIT=$?" > file` itself,
detach the process, and poll that file.

**Supersedes:** nothing. **Related:** `docs/proposals/heartbeat-*` and the
B5-0613 census-tooling family, which are the same "the tool must be able to
fail visibly" concern applied to PowerShell helpers.
