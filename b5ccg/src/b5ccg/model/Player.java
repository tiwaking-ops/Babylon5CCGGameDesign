package b5ccg.model;

import b5ccg.model.enums.*;
import java.util.*;

public class Player {
    private final String  name;
    private final Faction faction;
    private final boolean isHuman;

    private int influence = 4;
    private int appliedPool = 4;

    private CharacterCard        ambassador;

    // B5-0357/B5-0366: stat bonus registry — one per player, the single
    // source of truth for non-permanent (and currently permanent) stat
    // bonuses. Replaces the permanent mutation in CharacterCard.applyStatDelta
    // and FleetCard.applyMilitaryDelta. Bonuses are keyed by sourceCardId for
    // remove-by-source (blanking/discard) and swept at the round boundary.
    private final List<StatBonus> bonuses = new ArrayList<StatBonus>();
    private final List<CharacterCard>  innerCircle     = new ArrayList<CharacterCard>();
    private final Set<CharacterCard> healedInnerCircleThisTurn = new HashSet<CharacterCard>();
    private final List<CharacterCard>  supportingRole  = new ArrayList<CharacterCard>();
    private final List<FleetCard>      fleets          = new ArrayList<FleetCard>();
    private final List<LocationCard>   locations       = new ArrayList<LocationCard>();
    private final List<GroupCard>      groups          = new ArrayList<GroupCard>();
    private final List<EnhancementCard> enhancements   = new ArrayList<EnhancementCard>();
    private       AgendaCard           agenda;

    private       GameState            gameState; // back-reference (B5-0376 Phase A)

    private final List<Card> hand = new ArrayList<Card>();
    private       Deck       deck;

    private boolean passed       = false;
    private int     actionsLeft  = 1;
    private boolean hasForfeited = false;

    // B5-0339 (rulebook §IV): 1 while the ambassador "may apply 1 influence
    // less than usual when sponsoring a card" later this turn — granted by
    // executing the assistant's sponsor-discount ability, consumed by the
    // first recruit and cleared with the turn at startRound.
    private int sponsorDiscount = 0;

    public Player(String name, Faction faction, boolean isHuman) {
        this.name     = name;
        this.faction  = faction;
        this.isHuman  = isHuman;
    }

    // ── Influence ────────────────────────────────────────────────────────────
    public int  getInfluence()          { return influence; }
    public int  getAppliedPool()        { return appliedPool; }
    public void gainInfluence(int n)    { if (n > 0) { influence += n; appliedPool += n; } }
    public void loseInfluence(int n) {
        if (n <= 0) return;
        int lost = Math.min(influence, n);
        influence -= lost;
        appliedPool = Math.max(0, appliedPool - lost);
    }
    public boolean applyInfluence(int n) {
        if (n < 0 || appliedPool < n) return false;
        appliedPool -= n;
        return true;
    }
    /** Compatibility name; spending always consumes the per-turn pool. */
    public boolean spendInfluence(int n) { return applyInfluence(n); }
    public void restoreAppliedPool() { appliedPool = influence; }

    // ── Identity ─────────────────────────────────────────────────────────────
    public String  getName()    { return name; }
    public Faction getFaction() { return faction; }
    public boolean isHuman()    { return isHuman; }

    // ── Ambassador ───────────────────────────────────────────────────────────
    public CharacterCard getAmbassador()              { return ambassador; }
    public void          setAmbassador(CharacterCard a) { ambassador = a; if (a != null) a.setOwner(this); }

    // ── Zones ────────────────────────────────────────────────────────────────
    public List<CharacterCard>   getInnerCircle()    { return innerCircle; }
    public List<CharacterCard>   getSupportingRole() { return supportingRole; }
    public List<FleetCard>       getFleets()         { return fleets; }
    public List<LocationCard>    getLocations()      { return locations; }
    public List<GroupCard>       getGroups()         { return groups; }
    public List<EnhancementCard> getEnhancements()   { return enhancements; }

    /** B5-0371: tracks IC heal actions until the end of the action round. */
    public void markInnerCircleHealed(CharacterCard ch) { healedInnerCircleThisTurn.add(ch); }
    public boolean allInnerCircleHealed() {
        if (innerCircle.isEmpty()) return false;
        return healedInnerCircleThisTurn.containsAll(innerCircle);
    }
    public void resetInnerCircleHealing() { healedInnerCircleThisTurn.clear(); }

    /** Links a card to this player's bonus registry when it enters a zone. */
    public void addCharacter(CharacterCard ch) { if (ch != null) { ch.setOwner(this); innerCircle.add(ch); } }
    public void addFleet(FleetCard fl) { if (fl != null) { fl.setOwner(this); fleets.add(fl); } }

    public boolean controlsCard(Card card) {
        return card != null && (card == ambassador || innerCircle.contains(card)
                || supportingRole.contains(card) || fleets.contains(card)
                || locations.contains(card) || groups.contains(card)
                || enhancements.contains(card) || agenda == card);
    }

    /** Clears the once-neutralized action lock at the next action-round start. */
    public void resetNeutralizedTurnLocks() {
        if (ambassador != null) ambassador.resetNeutralizedTurnLock();
        for (CharacterCard ch : innerCircle) ch.resetNeutralizedTurnLock();
        for (CharacterCard ch : supportingRole) ch.resetNeutralizedTurnLock();
        for (FleetCard fl : fleets) fl.resetNeutralizedTurnLock();
        for (LocationCard loc : locations) loc.resetNeutralizedTurnLock();
        for (GroupCard gr : groups) gr.resetNeutralizedTurnLock();
    }

    public AgendaCard getAgenda()           { return agenda; }
    public void       setAgenda(AgendaCard a) { agenda = a; }

    /** B5-0376 Phase A: back-reference for tension-matrix lookups in Conflict. */
    public GameState  getGameState()        { return gameState; }
    public void       setGameState(GameState s) { gameState = s; }

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
        if (type != ConflictType.MILITARY) {
            if (ambassador != null) ambassador.setOwner(this);
            if (ambassador != null && !ambassador.isFaceDown() && !ambassador.isRotated())
                total += ambassador.getPrimaryStatValue(type);
            for (CharacterCard ch : innerCircle) {
                ch.setOwner(this);
                if (ch == ambassador) continue; // already counted above (B5-0321 seat)
                if (!ch.isFaceDown() && !ch.isRotated())
                    total += ch.getPrimaryStatValue(type);
            }
        }
        if (type == ConflictType.MILITARY) {
            // B5-0337 (audit D5): Military totals are fleet-based only.
            // Rulebook (Action Details, Support or Oppose): "Characters do
            // not have the Military ability… one character per fleet may
            // rotate to add his Leadership Ability to the Military Ability
            // of any fleet" — character Leadership enters ONLY through the
            // fleet-leader relation (carried by the fleet's effective
            // value); every unled character's Leadership is excluded here.
            // Previously every IC character's Leadership fed the total
            // directly, double-counting it against the fleet values.
            for (FleetCard fl : fleets) {
                fl.setOwner(this);
                if (!fl.isRotated())
                    total += fl.getEffectiveMilitary();
            }
        }
        return total;
    }

     // ── Location income ──────────────────────────────────────────────────────
    public int collectLocationIncome() {
        int income = 0;
        for (LocationCard loc : locations) income += loc.getInfluencePerRound();
        gainInfluence(income);
        return income;
    }

    // ── Stat bonus registry (B5-0366) ──────────────────────────────────────────

    /** All bonuses belonging to this player (faction-scope + attached ones
     *  sourced by the player's cards). Unmodifiable view. */
    public List<StatBonus> getBonuses() {
        return Collections.unmodifiableList(bonuses);
    }

    /** Adds a bonus to this player's registry. */
    public void addBonus(StatBonus b) {
        bonuses.add(b);
    }

    /** Adds the bonus and returns it (for immediate-effect callers). */
    public StatBonus grantBonus(StatBonus b) {
        if (b == null) return null;
        if (!b.cumulative) {
            Iterator<StatBonus> old = bonuses.iterator();
            while (old.hasNext()) {
                StatBonus prior = old.next();
                if (prior.sourceCardId.equals(b.sourceCardId) && prior.stat == b.stat
                        && prior.scope == b.scope
                        && eq(prior.targetCardId, b.targetCardId)
                        && eq(prior.ownerName, b.ownerName)) old.remove();
            }
        }
        bonuses.add(b);
        return b;
    }

    private static boolean eq(Object a, Object b) { return a == null ? b == null : a.equals(b); }

    /** Removes turn-limited bonuses whose expiry boundary has been reached. */
    public int sweepBonusExpiries(int roundNumber) {
        int removed = 0;
        Iterator<StatBonus> it = bonuses.iterator();
        while (it.hasNext()) {
            StatBonus b = it.next();
            if ((b.expiry == Expiry.END_OF_TURN || b.expiry == Expiry.START_OF_NEXT_OWNER_TURN)
                    && b.createdRound < roundNumber) { it.remove(); removed++; }
        }
        return removed;
    }

    /**
     * Removes every bonus sourced by the named card id from this player's
     * registry (discard / blanking / leaving play). Returns how many were
     * removed.
     */
    public int removeBonusesBySource(String cardId) {
        if (cardId == null) return 0;
        int removed = 0;
        Iterator<StatBonus> it = bonuses.iterator();
        while (it.hasNext()) {
            if (it.next().sourceCardId.equals(cardId)) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }

    // ── B5-0468: attached-bonus probe for the opponent-targeted enhancement seam ──

    /**
     * B5-0468 probe: returns true if this player's bonus registry contains an
     * ATTACHED-scope bonus from the given source card targeting the given card.
     * Used by conformance tests to verify the seam closes correctly (grant → check).
     * Returns false if either argument is null (defensive).
     */
    public boolean hasAttachedBonusFrom(String sourceCardId, String targetCardId) {
        if (sourceCardId == null || targetCardId == null) return false;
        for (StatBonus b : bonuses) {
            if (b.scope == BonusScope.ATTACHED
                    && b.sourceCardId != null && b.sourceCardId.equals(sourceCardId)
                    && b.targetCardId != null && b.targetCardId.equals(targetCardId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Effective value of a card's stat, accounting for this player's attached
     * bonuses. The formula (B5-0357 proposal §3.2):
     *   effective = printedBase + sum(ATTACHED bonuses matching card+stat)
     * The computed overlays (B5-0337 leader, B5-0339 assistant) are NOT
     * applied here — they live inside the card's own getPrimaryStatValue.
     *
     * For ATTACHED-scope bonuses the targetCardId must match the given card's
     * id. For FACTION-scope bonuses the ownerName must match this player's name
     * AND the stat must match — but FACTION bonuses apply uniformly and are
     * read by the card's getPrimaryStatValue (which calls here). This method
     * only sums ATTACHED bonuses; faction-scope bonuses are handled by the
     * card reading its own player's registry.
     *
     * @param cardId the card being evaluated
     * @param stat   the stat key
     * @param printedBase the raw printed stat value
     * @return printedBase + applicable ATTACHED bonuses (pre-clamp)
     */
    public int effectiveStat(String cardId, StatKey stat, int printedBase) {
        return effectiveStat(cardId, stat, printedBase, false);
    }

    public int effectiveStat(String cardId, StatKey stat, int printedBase, boolean includeFaction) {
        int bonus = 0;
        int floor = 0;
        boolean psiUnlocked = false;
        if (stat == StatKey.PSI && printedBase == 0) {
            for (StatBonus b : bonuses) {
                if (b.stat == StatKey.PSI && b.psiFromZero && appliesTo(b, cardId, includeFaction)) {
                    psiUnlocked = true;
                    break;
                }
            }
        }
        for (StatBonus b : bonuses) {
            if (stat == b.stat && appliesTo(b, cardId, includeFaction)) {
                if (stat != StatKey.PSI || printedBase != 0 || psiUnlocked) bonus += b.delta;
                if (b.floor > floor) floor = b.floor;
            }
        }
        int raw = printedBase + bonus;
        if (floor > 0) {
            raw = Math.max(raw, Math.min(printedBase, floor));
        }
        return raw;
    }

    private boolean appliesTo(StatBonus b, String cardId, boolean includeFaction) {
        if (b.scope == BonusScope.ATTACHED)
            return b.targetCardId != null && b.targetCardId.equals(cardId);
        return includeFaction && b.scope == BonusScope.FACTION && name != null && name.equals(b.ownerName);
    }

    /**
     * Effective value of a card's stat including FACTION-scope bonuses for this
     * player. The full read path:
     *   effective = printedBase + sum(ATTACHED to card) + sum(FACTION owner=me)
     * Final clamp to 0 happens at the consumer; this returns the raw sum.
     */

    // ── Turn state ────────────────────────────────────────────────────────────
    public boolean isPassed()      { return passed; }
    public void    setPassed(boolean v) { passed = v; }
    public int     getActionsLeft()    { return actionsLeft; }
    public void    resetActions()      { actionsLeft = 1; passed = false; }
    public void    useAction()         { actionsLeft = Math.max(0, actionsLeft - 1); }
    public void    grantExtraAction()  { actionsLeft++; }

    // ── Assistant sponsor discount (B5-0339; rulebook §IV) ────────────
    public int getSponsorDiscount()               { return sponsorDiscount; }
    public void grantSponsorDiscount(int n)       { sponsorDiscount = Math.max(sponsorDiscount, n); }
    /** Consumes up to n points of discount; returns what was applied. */
    public int consumeSponsorDiscount(int n) {
        int used = Math.min(n, sponsorDiscount);
        sponsorDiscount -= used;
        return used;
    }

    @Override
    public String toString() {
        return name + " (" + faction + ") INF=" + influence;
    }
}
