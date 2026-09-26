# Reusable lesson: seam-first UI disables until ready

When a UI control depends on an engine seam whose eligibility is resolved externally (e.g. Censure-class via `GameAction.hasExplicitTarget()`, B5-0487), build the control to a known disabled/empty state and only populate/enable it from a refresh method that re-derives eligibility from live state — never cache eligibility at init time. This keeps the UI correct across state transitions (card change, phase change, player change) without duplicating engine logic. The control should render a clear empty-state message (e.g. "(no opponent fleets available)") when the derived list is empty, not an empty dropdown.

Applied in: B5-0487 (Censure opponent-fleet target picker, `b5ccg/src/b5ccg/ui/MainWindow.java`).
