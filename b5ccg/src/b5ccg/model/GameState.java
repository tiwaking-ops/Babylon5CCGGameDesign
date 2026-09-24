package b5ccg.model;

import b5ccg.model.enums.*;
import java.util.*;
import b5ccg.model.enums.TensionMatrix;

public class GameState {
    private final List<Player> players;
    private       int          currentPlayerIndex = 0;
    private       int          roundNumber        = 1;
    private       GamePhase    phase              = GamePhase.SETUP;
    private       Conflict     activeConflict;
    private       Player       winner;
    private final Set<Player> conflictsInitiatedThisTurn = new HashSet<Player>();

    // ── Non-player forces (B5-0340; rulebook :153 "Influence": the station,
    //    the Shadows and the Vorlons accumulate influence like players do).
    //    Shadow/Vorlon ratings exist so the Shadow-War trigger (rulebook
    //    :178) has a canonical home; nothing in the current engine or data
    //    moves them (B5-0354 research) — same rationale as the station's
    //    start-0 decision. isShadowWar() is the guard surface condition 2
    //    consults.
    private final Babylon5Station station = new Babylon5Station();
    private int shadowInfluence = 0;
    private int vorlonInfluence = 0;

    // B5-0338 (audit D4): aftermaths "in play" on a target, keyed by target
    // Player identity. The current engine resolves aftermath effects
    // immediately and discards the card, so nothing persists yet — the
    // registry exists so the persistent variant gets the one-named-per-target
    // guard the moment effects can attach; attachAftermath is authoritative.
    private final Map<Player, List<AftermathCard>> attachedAftermaths = new LinkedHashMap<Player, List<AftermathCard>>();

    private final List<String> log = new ArrayList<String>();

    public GameState(List<Player> players) {
        this.players = new ArrayList<Player>(players);
    }

    // ── Players ───────────────────────────────────────────────────────────────
    public List<Player> getPlayers()       { return Collections.unmodifiableList(players); }
    public Player getActivePlayer()        { return players.get(currentPlayerIndex); }
    public Player getHumanPlayer() {
        for (Player p : players) {
            if (p.isHuman()) return p;
        }
        return players.get(0);
    }

    public void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    // ── Round ─────────────────────────────────────────────────────────────────
    public int       getRoundNumber() { return roundNumber; }
    public void      advanceRound()   { roundNumber++; currentPlayerIndex = 0; conflictsInitiatedThisTurn.clear(); for (Player p : players) p.sweepBonusExpiries(roundNumber); }

    // ── Phase ─────────────────────────────────────────────────────────────────
    public GamePhase getPhase()          { return phase; }
    public void      setPhase(GamePhase p) { phase = p; log("Phase → " + p); }

    // ── Conflict ──────────────────────────────────────────────────────────────
    public Conflict getActiveConflict()              { return activeConflict; }
    public void     setActiveConflict(Conflict c)    { activeConflict = c; }
    public void     clearActiveConflict()            { activeConflict = null; }

    // ── One-conflict-per-turn tracking (B5-0302; rulebook "Conflicts":
    //    "Each faction may normally initiate only one conflict per turn")
    public boolean hasInitiatedConflictThisTurn(Player p) { return conflictsInitiatedThisTurn.contains(p); }
    public void    markConflictInitiated(Player p)        { conflictsInitiatedThisTurn.add(p); }

    // ── War conflict tracking (B5-0376) ─────────────────────────────────────
    private final Set<Player> warConflictsInitiatedThisTurn = new HashSet<Player>();
    public boolean hasInitiatedWarConflictThisTurn(Player p) { return warConflictsInitiatedThisTurn.contains(p); }
    public void    markWarConflictInitiated(Player p)        { warConflictsInitiatedThisTurn.add(p); }
    public void    clearWarConflictInitiated(Player p)       { warConflictsInitiatedThisTurn.remove(p); }

    // ── Tension matrix (B5-0376; proposal §2.1) ─────────────────────────────
    private final TensionMatrix tensionMatrix = new TensionMatrix();
    public TensionMatrix getTensionMatrix()               { return tensionMatrix; }
    public boolean        isAtWar(Faction a, Faction b)   { return tensionMatrix.isAtWar(a, b); }
    public boolean        isAtWar(Faction faction)        {
        // true when faction is at war with any other faction
        for (Player p : players) {
            if (p.getFaction() != null && p.getFaction() != faction
                    && tensionMatrix.isAtWar(faction, p.getFaction())) return true;
        }
        return false;
    }
    public void raiseTension(Faction source, Faction target, int delta) {
        tensionMatrix.raiseTension(source, target, delta);
    }
    /** Returns the player who controls the given location, or null. */
    /** B5-0376 Phase B: current controller of a location — the capturing
     *  occupier when captured, else the owning player in a locations list. */
    public Player findLocationOwner(LocationCard loc) {
        Player capturer = loc.getCapturedBy();
        if (capturer != null) return capturer;
        for (Player p : players) {
            if (p.getLocations().contains(loc)) return p;
        }
        return null;
    }

    // ── Aftermaths in play on a target (B5-0338; rulebook Aftermath Cards,
    //    Bonuses and Blanking: "only one of each named aftermath may be in
    //    play on the same target" — audit D4). Empty until persistent
    //    effects exist; the attach guard is authoritative the moment they do.
    public List<AftermathCard> getAttachedAftermaths(Player target) {
        List<AftermathCard> l = attachedAftermaths.get(target);
        return l != null ? l : Collections.<AftermathCard>emptyList();
    }

    /** True when the named aftermath is NOT already in play on the target. */
    public boolean canAttachAftermath(AftermathCard a, Player target) {
        for (AftermathCard in : getAttachedAftermaths(target)) {
            if (in.getTitle().equals(a.getTitle())) return false;
        }
        return true;
    }

    /**
     * Attaches the aftermath to the target, enforcing D4 (one of each named
     * aftermath per target). Returns false (state unchanged) when a same-named
     * aftermath is already attached.
     */
    public boolean attachAftermath(AftermathCard a, Player target) {
        if (!canAttachAftermath(a, target)) return false;
        List<AftermathCard> l = attachedAftermaths.get(target);
        if (l == null) {
            l = new ArrayList<AftermathCard>();
            attachedAftermaths.put(target, l);
        }
        l.add(a);
        return true;
    }

    // ── Contingencies in play (B5-0394) ─────────────────────────────────────
    /** Returns all face-down contingencies placed by the given player. */
    public List<ContingencyCard> getPlacedContingencies(Player p) {
        List<ContingencyCard> result = new ArrayList<ContingencyCard>();
        if (p == null) return result;
        collectContingenciesFromZone(p.getInnerCircle(), p, result);
        collectContingenciesFromZone(p.getSupportingRole(), p, result);
        collectContingenciesFromZone(p.getFleets(), p, result);
        collectContingenciesFromZone(p.getLocations(), p, result);
        collectContingenciesFromZone(p.getGroups(), p, result);
        collectContingenciesFromZone(p.getEnhancements(), p, result);
        Card agenda = p.getAgenda();
        if (agenda != null) collectContingenciesFromCard(agenda, p, result);
        return result;
    }

    /** Returns all face-down contingencies on the board (all players). */
    public List<ContingencyCard> getAllPlacedContingencies() {
        List<ContingencyCard> result = new ArrayList<ContingencyCard>();
        for (Player p : players) {
            result.addAll(getPlacedContingencies(p));
        }
        return result;
    }

    private void collectContingenciesFromZone(List<? extends Card> zone, Player owner, List<ContingencyCard> out) {
        if (zone == null) return;
        for (Card c : zone) {
            collectContingenciesFromCard(c, owner, out);
        }
    }

    private void collectContingenciesFromCard(Card c, Player owner, List<ContingencyCard> out) {
        if (c == null) return;
        List<ContingencyCard> contingencies = c.getContingencies();
        if (contingencies != null) {
            for (ContingencyCard cc : contingencies) {
                if (cc.getPlayedBy() == owner && !cc.isRevealed()) {
                    out.add(cc);
                }
            }
        }
    }

    // ── Mercenaries (B5-0395; rulebook §Mercenaries :735–:741) ──────────────
    // "Some cards in the game may be used each turn only by the player who
    // applies the most influence to control them." Control is per-turn:
    // bids accumulate through the ACTION phase, the highest cumulative total
    // controls at the MERCENARY phase, and everything clears at the next
    // round boundary (startRound). The offer list is a game-setup surface
    // (currently hydratable only by tests/synthetic fixtures — the pool
    // carries zero mercenary cards per B5-0386). Interpretation recorded in
    // DECISIONS: bids are turn-ordered actions (each bid is an action during
    // ACTION), cumulative per player; a tie for the highest total crowns
    // NOBODY (the mercenary does not act) — the rulebook is silent, and the
    // no-controller default matches the D12 "ties crown nobody" discipline.
    private final List<Card> mercenaryOffers = new ArrayList<Card>();
    private final Map<Card, Map<Player, Integer>> mercenaryBids =
            new LinkedHashMap<Card, Map<Player, Integer>>();
    private final Map<Card, Player> mercenaryControllers =
            new LinkedHashMap<Card, Player>();

    /** The cards currently offered for bidding (unmodifiable). */
    public List<Card> getMercenaryOffers() {
        return Collections.unmodifiableList(mercenaryOffers);
    }

    /** Offers a mercenary-flagged card for bidding this game. Refuses
     *  non-mercenary cards, nulls and duplicates (loudly for non-mercenary —
     *  a data/reference error the engine must not hide). */
    public boolean addMercenaryOffer(Card c) {
        if (c == null || mercenaryOffers.contains(c)) return false;
        if (!c.isMercenary()) {
            System.err.println("B5-0395: refused to offer non-mercenary card "
                    + c.getId() + " (mercenary flag absent).");
            return false;
        }
        mercenaryOffers.add(c);
        return true;
    }

    public boolean isMercenaryInPlay(Card c) {
        return c != null && mercenaryOffers.contains(c);
    }

    /** Cumulative total a single player has bid on c this turn. */
    public int getMercenaryBid(Card c, Player p) {
        if (c == null || p == null) return 0;
        Map<Player, Integer> per = mercenaryBids.get(c);
        Integer v = (per == null) ? null : per.get(p);
        return v == null ? 0 : v.intValue();
    }

    /** Total influence bid on c across all players this turn. */
    public int totalMercenaryBids(Card c) {
        if (c == null) return 0;
        Map<Player, Integer> per = mercenaryBids.get(c);
        if (per == null) return 0;
        int total = 0;
        for (Integer v : per.values()) total += v.intValue();
        return total;
    }

    /** Records a cumulative bid (amount > 0) from p on c. Returns false when
     *  the card is not offered or the amount is non-positive (engine callers
     *  gate affordability before reaching here). */
    public boolean placeMercenaryBid(Card c, Player p, int amount) {
        if (c == null || p == null || amount <= 0 || !isMercenaryInPlay(c)) return false;
        Map<Player, Integer> per = mercenaryBids.get(c);
        if (per == null) {
            per = new LinkedHashMap<Player, Integer>();
            mercenaryBids.put(c, per);
        }
        int prior = per.containsKey(p) ? per.get(p).intValue() : 0;
        per.put(p, Integer.valueOf(prior + amount));
        return true;
    }

    /** Rolls back the most recent `amount` bid from p on c — used only by the
     *  engine's defensive spend-failure path in executeBidOnMercenary. */
    public void placeMercenaryBidRollback(Card c, Player p, int amount) {
        if (c == null || p == null || amount <= 0) return;
        Map<Player, Integer> per = mercenaryBids.get(c);
        if (per == null || !per.containsKey(p)) return;
        int now = per.get(p).intValue();
        if (now <= amount) {
            per.remove(p);
        } else {
            per.put(p, Integer.valueOf(now - amount));
        }
    }

    /** Resolves control for every offered mercenary at the MERCENARY phase:
     *  the single player with the strictly-highest cumulative bid controls;
     *  ties (or no bids) crown nobody. Returns the resolved controllers as a
     *  snapshot copy for safe iteration while the phase runs. */
    public Map<Card, Player> resolveMercenaries() {
        Map<Card, Player> resolved = new LinkedHashMap<Card, Player>();
        for (Card c : mercenaryOffers) {
            Player controller = null;
            int best = 0;
            boolean tie = false;
            for (Player p : players) {
                int bid = getMercenaryBid(c, p);
                if (bid > best) {
                    best = bid;
                    controller = p;
                    tie = false;
                } else if (bid == best && bid > 0) {
                    tie = true;
                }
            }
            if (tie || best <= 0) controller = null;
            if (controller != null) {
                mercenaryControllers.put(c, controller);
                resolved.put(c, controller);
            }
        }
        return resolved;
    }

    /** The controller of mercenary c for the current turn (after resolution),
     *  or null when none (tie / no bids / not yet resolved). */
    public Player getMercenaryController(Card c) {
        return mercenaryControllers.get(c);
    }

    /** Current controllers for all resolved mercenaries (unmodifiable). */
    public Map<Card, Player> getMercenaryControllers() {
        return Collections.unmodifiableMap(mercenaryControllers);
    }

    /** Clears bids + control for a new turn; the offer list persists (it is a
     *  game-setup surface). Called from RulesEngine.startRound. */
    public void clearMercenaryState() {
        mercenaryBids.clear();
        mercenaryControllers.clear();
    }

    // ── Non-player forces (B5-0340) ───────────────────────────────────────
    /** The Babylon 5 station (rulebook :153); exactly one per game. */
    public Babylon5Station getStation()   { return station; }
    public int  getShadowInfluence()      { return shadowInfluence; }
    public int  getVorlonInfluence()      { return vorlonInfluence; }
    /** Mutation surface for the future Shadow/Vorlon influence wiring; no
     *  current card or rule moves these ratings (B5-0354 research). */
    public void setShadowInfluence(int n) { shadowInfluence = Math.max(0, n); }
    public void setVorlonInfluence(int n) { vorlonInfluence = Math.max(0, n); }
    /**
     * Rulebook :178 — Shadow or Vorlon influence 20+ begins the Shadow War,
     * during which NO Standard Victory is possible. Nothing triggers it in
     * the current engine (both ratings are always 0 today); Standard Victory
     * condition 2 consults this guard from day one so the rule cannot be
     * violated the moment a trigger exists.
     */
    public boolean isShadowWar() {
        return shadowInfluence >= Babylon5Station.CONDITION_2_THRESHOLD
            || vorlonInfluence >= Babylon5Station.CONDITION_2_THRESHOLD;
    }

    // ── Victory ───────────────────────────────────────────────────────────────
    public Player  getWinner()            { return winner; }
    public boolean isGameOver()           { return winner != null; }
    public void    setWinner(Player p)    { winner = p; log("WINNER: " + p.getName()); }

    // ── Log ───────────────────────────────────────────────────────────────────
    public void log(String msg)          { log.add("[R" + roundNumber + "] " + msg); }
    public List<String> getLog()         { return Collections.unmodifiableList(log); }
    public String getLastLogEntry()      { return log.isEmpty() ? "" : log.get(log.size() - 1); }
}
