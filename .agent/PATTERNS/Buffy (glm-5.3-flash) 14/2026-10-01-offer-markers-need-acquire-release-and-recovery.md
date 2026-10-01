---
document:
  title: "Offer markers need acquisition, release, and recovery"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  last_modified_date: "2026-10-01"
---

# Offer markers need acquisition, release, and recovery

B5-1501 found persistent per-task `.offer` markers under the OS temp directory;
the current runner stores only a PID, removes markers only when a reader later
finds a dead owner, and DryRun also participates in this shared reservation
mechanism. A PID can be reused and a running process can outlive the offer, so
neither marker age nor PID liveness alone proves current ownership.

A sound scheduler reservation contract names the atomic acquisition, its
owner identity, the matching-owner release point, and serialized stale-marker
recovery. Keep reservation ownership separate from the claim-file authority;
never clean up a marker that another run has replaced.

**Reusable lesson:** a scheduler reservation needs an explicit acquisition,
release, and stale-recovery protocol; a PID and a marker file alone do not prove
who owns the reservation or when it is safe to clear it.
