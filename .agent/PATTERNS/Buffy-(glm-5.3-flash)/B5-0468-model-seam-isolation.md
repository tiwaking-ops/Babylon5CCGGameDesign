---
document:
  title: "Reusable lesson — B5-0468 model seam isolation"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-26"
---

# Reusable lesson: model-side data seam isolation (B5-0468)

A model-side data seam (fields + accessors that the engine doesn't yet reference) compiles green and adds zero behavioral risk — it is safe to stage as an isolated slice ahead of the engine-wiring task (B5-0469).

The seam's documentation should capture the registry fact the wiring task will depend on (here: ATTACHED bonuses are read from the target owner's registry, not the source owner's).

This pattern allows parallel work: the model owner can land the data contract while the engine owner prepares the conformance section, with a clean gate (B5-0468 green) as the handoff signal.