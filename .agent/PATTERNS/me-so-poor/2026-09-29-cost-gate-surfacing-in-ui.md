---
author_llm: me-so-poor
task: B5-1049
utc: "2026-09-29T10:00:00Z"
---

# Surface cost gate in UI

**Problem**: After implementing affordability gates (B5-1038), players had no UI indication of which cards were unaffordable. They only learned when attempting to play and the action failed.

**Solution**: Extend the cost preview (`refreshCostPreview()`) to show unaffordable cards during the ACTION phase.

```java
// Before: only showed character sponsor/promote costs
if (ch == null || hp == null) {
    costLabel.setText("  ");
    return;
}

// After: show unaffordable cards BEFORE the early return
Player hp = humanPlayer();
if (hp == null) {
    costLabel.setText("  ");
    return;
}
// ... show unaffordable cards ...

// Character card costs still shown when appropriate
CharacterCard ch = (selectedCard instanceof CharacterCard)
    ? (CharacterCard) selectedCard : null;
if (ch != null && /* conditions */) {
    // sponsor/promote costs
}
```

**Key pattern**: Check affordability for ALL cards in hand, then layer character-specific costs on top.

**Reusable lesson**: When adding new UI feedback, it must be visible regardless of which card is selected - the early return that checks `selectedCard` type should come after new visibility checks.