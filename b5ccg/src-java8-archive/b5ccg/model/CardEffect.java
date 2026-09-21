package b5ccg.model;

@FunctionalInterface
public interface CardEffect {
    void apply(GameState state, Player owner, Card source);
}
