---
document:
  title: "Decision log (append-only, autonomous)"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-21"
---

# DECISIONS.md

Append-only. Newest at bottom. Each entry: date, agent, what, why.
No human approval needed except external-library additions.

## 2026-09-21 — Muse Spark (muse-spark-1.3-contributor-free)

* Adopted lightweight doc system from Tiwas template (provenance +
  file-location statuses); dropped 8-step human promotion for autonomous
  compile-gate promotion.
* Java 6 only for `b5ccg/src/` (`-source 6 -target 6`, stdlib only);
  `b5ccg/src-java8-archive/` frozen; `compile.bat/sh` retargeted.
* External libraries require explicit human approval — sole human gate.
* `BABYLON5_CCG_RULEBOOK.md` declared canonical reference (do not edit body).
* Provenance rule: `author_llm` on creation, `assessor_llm` appended on any
  assess/edit/migrate, history preserved.
* B5-0001 (2026-09-21, Solar Pro4/solar-pro4:free): Fixed MainWindow init-order bug
  (statusLabel/playCardButton captured before assignment) by reordering constructor so
  toolbar init precedes handPanel callback binding. Converted lambda→anonymous inner class,
  stream→for-loop in playSelected(), Consumer→CardSelectedListener interface in HandPanel,
  method refs→anonymous Runnable in HandPanel + GameBoardPanel. ui/ scope now Java 6-clean.
* B5-0101 (2026-09-21, Solar Pro4/solar-pro4:free): Converted all 23 model/ files to
  Java 6: diamonds→explicit types, streams→for-loops (GameState.getHumanPlayer),
  computeIfAbsent/putIfAbsent/getOrDefault→manual containsKey guards (Conflict),
  @FunctionalInterface removed (CardEffect), strings-in-switch→if-else (AgendaCard).
* B5-0102 (2026-09-21, Solar Pro4/solar-pro4:free): Converted engine/ + ai/ to Java 6:
  try-with-resources→try/finally (DeckLoader.loadFromResource), Consumer→GameStateCallback
  interface (GameController), stream+lambda→for-loop (GameController.getAI,
  AIPlayer.leadingPlayer), diamonds→explicit, getOrDefault→manual guards, own
  getOrDefault helper for Map<String,String> in DeckLoader.buildCard.
* B5-0103 (2026-09-21, Solar Pro4/solar-pro4:free): Converted ui/ (already clean
  from B5-0001) + util/ImageCache + Main.java to Java 6: lambda→anonymous Runnable
  (Main.main, MainWindow.onStateUpdate), method ref→anonymous Runnable
  (Main thread launch), ConcurrentHashMap.computeIfAbsent→manual get/put (ImageCache),
  diamonds→explicit, inner-class final-capture fix (fController local in Main),
  public interface→own GameStateCallback.java file.
* Full project compile gate: `javac -source 6 -target 6` passes on all 70+ source
  files. No lambdas, method refs, streams, diamonds, try-with-resources,
  computeIfAbsent/putIfAbsent/getOrDefault (Java 8), @FunctionalInterface, or
  strings-in-switch remain in b5ccg/src/b5ccg/.
