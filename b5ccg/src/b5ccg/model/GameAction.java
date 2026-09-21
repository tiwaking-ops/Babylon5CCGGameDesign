package b5ccg.model;

public class GameAction {
    public enum Type {
        PLAY_CARD,
        INITIATE_CONFLICT,
        JOIN_CONFLICT_SUPPORT,
        JOIN_CONFLICT_OPPOSE,
        PLAY_AFTERMATH,
        RECRUIT_CHARACTER,
        PASS
    }

    private final Type   type;
    private final Card   card;
    private final Player target;

    public GameAction(Type type, Card card, Player target) {
        this.type   = type;
        this.card   = card;
        this.target = target;
    }

    public static GameAction pass()                         { return new GameAction(Type.PASS, null, null); }
    public static GameAction playCard(Card c)               { return new GameAction(Type.PLAY_CARD, c, null); }
    public static GameAction initiateConflict(Card c, Player t) { return new GameAction(Type.INITIATE_CONFLICT, c, t); }
    public static GameAction joinSupport()                  { return new GameAction(Type.JOIN_CONFLICT_SUPPORT, null, null); }
    public static GameAction joinOppose()                   { return new GameAction(Type.JOIN_CONFLICT_OPPOSE, null, null); }
    public static GameAction recruitCharacter(Card c)       { return new GameAction(Type.RECRUIT_CHARACTER, c, null); }

    public Type   getType()   { return type; }
    public Card   getCard()   { return card; }
    public Player getTarget() { return target; }

    @Override
    public String toString() {
        return type + (card != null ? ":" + card.getTitle() : "")
             + (target != null ? "→" + target.getName() : "");
    }
}
