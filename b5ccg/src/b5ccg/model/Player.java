package b5ccg.model;

import b5ccg.model.enums.*;
import java.util.*;

public class Player {
    private final String  name;
    private final Faction faction;
    private final boolean isHuman;

    private int influence = 4;

    private CharacterCard        ambassador;
    private final List<CharacterCard>  innerCircle     = new ArrayList<CharacterCard>();
    private final List<CharacterCard>  supportingRole  = new ArrayList<CharacterCard>();
    private final List<FleetCard>      fleets          = new ArrayList<FleetCard>();
    private final List<LocationCard>   locations       = new ArrayList<LocationCard>();
    private final List<GroupCard>      groups          = new ArrayList<GroupCard>();
    private final List<EnhancementCard> enhancements   = new ArrayList<EnhancementCard>();
    private       AgendaCard           agenda;

    private final List<Card> hand = new ArrayList<Card>();
    private       Deck       deck;

    private boolean passed       = false;
    private int     actionsLeft  = 1;
    private boolean hasForfeited = false;

    public Player(String name, Faction faction, boolean isHuman) {
        this.name     = name;
        this.faction  = faction;
        this.isHuman  = isHuman;
    }

    // ── Influence ────────────────────────────────────────────────────────────
    public int  getInfluence()          { return influence; }
    public void gainInfluence(int n)    { influence += n; }
    public void loseInfluence(int n)    { influence = Math.max(0, influence - n); }
    public boolean spendInfluence(int n) {
        if (influence < n) return false;
        influence -= n;
        return true;
    }

    // ── Identity ─────────────────────────────────────────────────────────────
    public String  getName()    { return name; }
    public Faction getFaction() { return faction; }
    public boolean isHuman()    { return isHuman; }

    // ── Ambassador ───────────────────────────────────────────────────────────
    public CharacterCard getAmbassador()              { return ambassador; }
    public void          setAmbassador(CharacterCard a) { ambassador = a; }

    // ── Zones ────────────────────────────────────────────────────────────────
    public List<CharacterCard>   getInnerCircle()    { return innerCircle; }
    public List<CharacterCard>   getSupportingRole() { return supportingRole; }
    public List<FleetCard>       getFleets()         { return fleets; }
    public List<LocationCard>    getLocations()      { return locations; }
    public List<GroupCard>       getGroups()         { return groups; }
    public List<EnhancementCard> getEnhancements()   { return enhancements; }

    public AgendaCard getAgenda()           { return agenda; }
    public void       setAgenda(AgendaCard a) { agenda = a; }

    // ── Hand & Deck ──────────────────────────────────────────────────────────
    public List<Card> getHand() { return hand; }
    public Deck       getDeck() { return deck; }
    public void       setDeck(Deck d) { deck = d; }

    public void addToHand(Card c)      { hand.add(c); }
    public void removeFromHand(Card c) { hand.remove(c); }

    /**
     * Draws n cards. When the draw pile is empty, the rulebook (Draw Round,
     * Step 3) forbids reshuffling: the player must instead discard one Inner
     * Circle character, and loses the game if he cannot (the ambassador may
     * never be discarded). Sets hasForfeited() instead of throwing.
     */
    public void drawCards(int n) {
        for (int i = 0; i < n; i++) {
            Card c = (deck == null) ? null : deck.draw();
            if (c == null) {
                // Deck-out: discard one non-ambassador Inner Circle character.
                CharacterCard discardMe = null;
                for (CharacterCard ch : innerCircle) {
                    if (ch != ambassador) { discardMe = ch; break; }
                }
                if (discardMe != null) {
                    innerCircle.remove(discardMe);
                    if (deck != null) deck.discard(discardMe);
                } else {
                    hasForfeited = true; // no Inner Circle character left to discard
                    return;
                }
            }
            if (c != null) hand.add(c);
        }
    }

    /** True once this player could not draw and had no Inner Circle
     *  character left to discard (rulebook Draw Round Step 3: he loses). */
    public boolean hasForfeited() { return hasForfeited; }

    // ── Inner Circle management ──────────────────────────────────────────────
    public void recruitToInnerCircle(CharacterCard ch) {
        supportingRole.remove(ch);
        if (!innerCircle.contains(ch)) innerCircle.add(ch);
    }

    public void placeInSupportingRole(CharacterCard ch) {
        innerCircle.remove(ch);
        if (!supportingRole.contains(ch)) supportingRole.add(ch);
    }

    // ── Total stat contribution in a conflict ─────────────────────────────────
    public int conflictTotal(ConflictType type) {
        int total = 0;
        if (ambassador != null && !ambassador.isFaceDown() && !ambassador.isRotated())
            total += ambassador.getPrimaryStatValue(type);
        for (CharacterCard ch : innerCircle)
            if (!ch.isFaceDown() && !ch.isRotated())
                total += ch.getPrimaryStatValue(type);
        if (type == ConflictType.MILITARY)
            for (FleetCard fl : fleets)
                if (!fl.isRotated())
                    total += fl.getMilitary();
        return total;
    }

    // ── Location income ──────────────────────────────────────────────────────
    public int collectLocationIncome() {
        int income = 0;
        for (LocationCard loc : locations) income += loc.getInfluencePerRound();
        gainInfluence(income);
        return income;
    }

    // ── Turn state ────────────────────────────────────────────────────────────
    public boolean isPassed()      { return passed; }
    public void    setPassed(boolean v) { passed = v; }
    public int     getActionsLeft()    { return actionsLeft; }
    public void    resetActions()      { actionsLeft = 1; passed = false; }
    public void    useAction()         { actionsLeft = Math.max(0, actionsLeft - 1); }
    public void    grantExtraAction()  { actionsLeft++; }

    @Override
    public String toString() {
        return name + " (" + faction + ") INF=" + influence;
    }
}
