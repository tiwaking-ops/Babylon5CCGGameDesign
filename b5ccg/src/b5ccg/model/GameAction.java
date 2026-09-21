package b5ccg.model;

import b5ccg.model.enums.*;

public class GameAction {
    public enum Type {
        PLAY_CARD,
        INITIATE_CONFLICT,
        JOIN_CONFLICT_SUPPORT,
        JOIN_CONFLICT_OPPOSE,
        PLAY_AFTERMATH,
        RECRUIT_CHARACTER,
        BUILD_INFLUENCE,
        PROMOTE_CHARACTER,
        PASS
    }

    private final Type   type;
    private final Card   card;
    private final Player target;
    // B5-0321: the rotating Inner Circle member that performs the action
    // (promotion needs BOTH the promoted character and the rotating member).
    private final CharacterCard leader;

    public GameAction(Type type, Card card, Player target) {
        this(type, card, target, null);
    }

    public GameAction(Type type, Card card, Player target, CharacterCard leader) {
        this.type   = type;
        this.card   = card;
        this.target = target;
        this.leader = leader;
    }

    // ── Factory methods ──────────────────────────────────────────────────────

    public static GameAction pass()                              { return new GameAction(Type.PASS, null, null); }
    public static GameAction playCard(Card c)                    { return new GameAction(Type.PLAY_CARD, c, null); }
    public static GameAction initiateConflict(Card c, Player t)  { return new GameAction(Type.INITIATE_CONFLICT, c, t); }
    public static GameAction joinSupport()                       { return new GameAction(Type.JOIN_CONFLICT_SUPPORT, null, null); }
    public static GameAction joinOppose()                        { return new GameAction(Type.JOIN_CONFLICT_OPPOSE, null, null); }
    public static GameAction recruitCharacter(Card c)            { return new GameAction(Type.RECRUIT_CHARACTER, c, null); }
    public static GameAction buildInfluence(CharacterCard leader){ return new GameAction(Type.BUILD_INFLUENCE, leader, null); }
    /** B5-0321: promote ch into the Inner Circle, rotating the IC member leader. */
    public static GameAction promoteCharacter(CharacterCard ch, CharacterCard leader) {
        return new GameAction(Type.PROMOTE_CHARACTER, ch, null, leader);
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public Type   getType()   { return type; }
    public Card   getCard()   { return card; }
    public Player getTarget() { return target; }
    public CharacterCard getLeader() { return leader; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        if (card != null) {
            sb.append(": ").append(card.getTitle());
        }
        if (target != null) {
            sb.append(" -> ").append(target.getName());
        }
        if (leader != null) {
            sb.append(" (leader: ").append(leader.getTitle()).append(")");
        }
        return sb.toString();
    }
}
