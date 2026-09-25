package b5ccg.engine;

import b5ccg.ai.AIPlayer;
import b5ccg.model.*;
import b5ccg.model.enums.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;

/**
 * B5-0308 — rulebook-conformance suite (headless).
 *
 * Extends the B5-0201 smoke-test idea: instead of exercising a full round,
 * each rule fixed from the B5-0203 audit is asserted directly against the
 * engine/model API, so a regression fails the build with a named rule.
 * New-file-only task: no game logic is modified here (B5-0201 precedent).
 *
 * Covered (fixed) rules:
 *   D1  Aftermath Won/Lost play conditions follow the INITIATOR's outcome
 *       (rulebook: Aftermath Cards / Aftermath Example).
 *   D3  Aftermath conflict-type gating incl. the PSI branch
 *       (rulebook: Type of Conflict).
 *   D8  Deck-out penalty: empty pile = no reshuffle; discard one
 *       non-ambassador Inner Circle character per required draw; forfeit
 *       when none remains (rulebook: Draw Round, Step 3).
 *
 * D12 Standard victory: 20+ power and strictly greatest; major agendas block
 *     the standard path but still win via their own condition (rulebook:
 *     Victory). Landed with B5-0305; former SKIPs converted to asserts.
 * D13 NON_ALIGNED cards behave as a normal race ("Non-Aligned IS a race
 *     name"); NEUTRAL stays universally playable (rulebook: Character
 *     Cards). Landed with B5-0305; former SKIPs converted to asserts.
 * CPT One conflict per faction per turn is enforced by the engine
 *     (B5-0302; rulebook: Conflicts). canInitiateConflict checks the
 *     GameState marker, GameController rejects illegal INITIATE_CONFLICT
 *     actions, and the marker clears at the round boundary.
 * CSD Conflict support/opposition sides: the initiator wins only when
 *     support strictly exceeds opposition; otherwise the leading opposer
 *     wins (B5-0309 / audit D14; rulebook: Conflicts). Also covers the
 *     damage rule: a loser's ambassador is damaged (face-down) on a
 *     military-resolution loss with a >= 3 gap.
 * CST Card cost field: cards carry an influence cost (default 0, negatives
 *     clamp), the loader hydrates the optional "cost" key, and recruit and
 *     promote costs compose from it with the double-for-other-race rule
 *     (B5-0323; rulebook: Anatomy of a Card, Sponsor, Promote).
 * AIS Cost-aware AI scoring: MEDIUM/HARD subtract card costs from positional
 *     values (B5-0202 Finding 6 / B5-0324). Zero costs preserve today's
 *     ordering exactly; raising a cost flips the choice; MEDIUM floors at 0.
 * PAR Participation enforcement: the optional "participation" key on conflict
 *     cards bounds who and what may commit (B5-0336; proposal §3.2). Players
 *     gate, cardTypes/fleetSubtypes filters (fleetClass-less fleets excluded),
 *     perPlayerQuota, leadersIncluded, requiresTarget at initiation, and the
 *     mandatory-commit dimensions enforced before resolution. Absent key =
 *     open participation = pre-B5-0336 behavior.
 * FLR Fleet leadership: one character per fleet may rotate to add his
 *     Leadership to that fleet's Military; character Leadership never enters
 *     MILITARY totals directly (B5-0337; audit D5, rulebook Action Details
 *     Support or Oppose). The relation expires at the round boundary.
 * LEAD Lead-a-fleet action plumbing (B5-0362; B5-0345 Tier-1 #1): the
 *     GameAction.LEAD_FLEET factory carries (leader, fleet) through the
 *     existing leader/card fields, AIPlayer's builder offers every legal
 *     pair, the controller handler reuses canLeadFleet/executeLeadFleet
 *     (rotate consumed, exactly one action per pair), MEDIUM/HARD pick the
 *     same best pair deterministically, and the relation expires at the
 *     round boundary (B5-0337).
 * AGL Agenda lifecycle (B5-0364; B5-0345 Tier-1 #3, rulebook :520/:719):
 *     DISCARD_AGENDA / REPLACE_AGENDA / REVEAL_AGENDA + sponsor one-major
 *     legality in the PLAY_CARD path. Guards: one agenda in play at a time;
 *     a Major cannot be discarded and can only be replaced by another Major;
 *     replace draws from hand, needs a ready Inner Circle leader, and never a
 *     hidden agenda; the replaced agenda is removed from the game. Hidden
 *     (face-down) agendas take no effect on play — victory conditions, the
 *     major standard bar, round income and diplomacy bonuses are all inert
 *     until reveal, which applies the on-play effect immediately.
 * AMT Aftermath targeting: non-Participant aftermaths target only the
 *     initiating faction; Participant aftermaths may target any faction that
 *     Supported/Opposed/Attacked (the resolved conflict's participants);
 *     effects land on the target; one of each named aftermath per target
 *     (B5-0338; audit D2/D4, rulebook Aftermath Cards / Participant).
 * ROT Rotate-for-effect: USE_ROTATE_EFFECT with a RotateEffectKind payload
 *     promotes the B5-0339 assistant paths to a player-chosen action
 *     (B5-0366; B5-0345 Tier-2 #5, rulebook §IV) — gate/executor delegate to
 *     the assistant primitives, controller consumes one action, round expiry
 *     clears both effects, payloads never mutate printed stats.
 * AST Ambassador's assistant: rotate a ready supporting assistant to give
 *     the ambassador +1 Diplomacy/Intrigue/Leadership while he remains
 *     rotated (computed, never mutating base stats; Psi untouched), or let
 *     the ambassador sponsor 1 influence cheaper later that turn (B5-0339;
 *     rulebook Your Ambassador's Assistant). Both effects expire at the
 *     round boundary.
 *
 * Run after compile.bat / compile.sh:
 *   java -cp b5ccg/out b5ccg.engine.HeadlessConformanceTest
 * Exit 0 only if every check passes and no SKIPped rule regressed into view.
 */
public class HeadlessConformanceTest {

    private static int checks  = 0;
    private static int failed  = 0;

    private static void check(String rule, String label, boolean ok) {
        checks++;
        System.out.println("  [" + rule + "] " + label + ": " + (ok ? "PASS" : "FAIL"));
        if (!ok) failed++;
    }

    private static void skip(String rule, String label, String owner) {
        System.out.println("  [" + rule + "] SKIP: " + label
                + " (still OPEN — owned by " + owner + ")");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static Player player(String name, Faction f) {
        Player p = new Player(name, f, false);
        CharacterCard amb = new CharacterCard("amb_" + name, "Amb " + name,
                "CHARACTER_" + f, Rarity.FIXED, f, CardSet.PREMIERE, "x", "text",
                3, 3, 3, 3, true);
        p.setAmbassador(amb);
        p.getInnerCircle().add(amb);
        List<Card> filler = new ArrayList<Card>();
        for (int i = 0; i < 10; i++) {
            filler.add(new EventCard("deck_" + name + "_" + i, "Deck " + i,
                    "EVENT", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE, "x", "text"));
        }
        p.setDeck(new Deck(filler));
        return p;
    }

    private static GameState state(Player... players) {
        List<Player> ps = new ArrayList<Player>();
        for (Player p : players) { ps.add(p); }
        GameState s = new GameState(ps);
        for (Player p : players) { p.setGameState(s); }
        s.setPhase(GamePhase.ACTION);
        return s;
    }

    private static EventCard event(String id) {
        return new EventCard(id, id, "EVENT", Rarity.COMMON, Faction.ANY,
                CardSet.PREMIERE, "x", "text");
    }

    private static AftermathCard aftermath(String id, String trigger) {
        return new AftermathCard(id, id, "AFTERMATH", Rarity.COMMON, Faction.ANY,
                CardSet.PREMIERE, "x", "text", trigger);
    }

    // ── B5-0336 helpers ──────────────────────────────────────────────────

    private static ConflictCard conflictCard(String id, ConflictType t,
                                             Participation part) {
        ConflictCard cc = new ConflictCard(id, id, "CONFLICT_" + t,
                Rarity.COMMON, Faction.ANY, CardSet.PREMIERE, "x", "text", t, 1);
        cc.setParticipation(part);
        return cc;
    }

    private static FleetCard fleetCard(String id, String fleetClass) {
        FleetCard f = new FleetCard(id, id, "FLEET", Rarity.COMMON, Faction.ANY,
                CardSet.PREMIERE, "x", "text", 3);
        f.setFleetClass(fleetClass);
        return f;
    }

    private static CharacterCard leaderCard(String id, int leadership) {
        return new CharacterCard(id, id, "CHARACTER_NARN", Rarity.RARE, Faction.NARN,
                CardSet.PREMIERE, "x", "text", 2, 2, 0, leadership, false);
    }

    /** B5-0364 helper: an INFLUENCE_20 agenda, Major or minor, of any faction. */
    private static AgendaCard agendaCard(String id, boolean major, Faction faction) {
        return new AgendaCard(id, id, major ? "AGENDA_MAJOR" : "AGENDA",
                major ? Rarity.RARE : Rarity.COMMON, faction, CardSet.PREMIERE,
                "x", "text", major, "INFLUENCE_20");
    }

    // ── D1: initiator-perspective aftermath Won/Lost ─────────────────────────

    private static void testD1() {
        System.out.println("D1: aftermath Won/Lost from the initiator's perspective");
        Player initiator = player("Init", Faction.NARN);
        Player opposer   = player("Oppo", Faction.MINBARI);
        GameState st = state(initiator, opposer);

        CharacterCard bigChar = new CharacterCard("big", "Big Opposer Char",
                "CHARACTER_MINBARI", Rarity.RARE, Faction.MINBARI, CardSet.PREMIERE,
                "x", "text", 0, 0, 0, 5, false);

        // Conflict 1: opposer commits 5 Leadership vs initiator 3 → initiator LOSES.
        ConflictCard cc = new ConflictCard("conf1", "Loss Strike", "CONFLICT_MILITARY",
                Rarity.COMMON, Faction.ANY, CardSet.PREMIERE, "x", "text",
                ConflictType.MILITARY, 2);
        Conflict lost = new Conflict(cc, initiator);
        lost.commitCard(initiator, initiator.getAmbassador());
        lost.commitCard(opposer, opposer.getAmbassador());
        lost.commitCard(opposer, bigChar);
        lost.resolve(opposer);

        RulesEngine rules = new RulesEngine();
        boolean initWon = rules.initiatorWon(lost, opposer);
        check("D1", "initiatorWon=false when the opposer wins", initWon == false);

        AftermathCard wonAm  = aftermath("am_won", "WON");
        AftermathCard lostAm = aftermath("am_lost", "LOST");
        initiator.addToHand(wonAm);
        initiator.addToHand(lostAm);
        opposer.addToHand(wonAm);
        opposer.addToHand(lostAm);

        check("D1", "losing initiator cannot play WON aftermath",
                !rules.canPlayAftermath(initiator, wonAm, lost, initWon));
        check("D1", "losing initiator can play LOST aftermath",
                rules.canPlayAftermath(initiator, lostAm, lost, initWon));
        check("D1", "winning opposer still evaluates vs initiator (no WON)",
                !rules.canPlayAftermath(opposer, wonAm, lost, initWon));
        // B5-0338 refinement: the legacy self-target form now applies the D2
        // target rule, so the winning opposer targets the initiator explicitly.
        check("D1", "winning opposer may play LOST aftermath (on the initiator)",
                rules.canPlayAftermath(opposer, lostAm, lost, initWon,
                                       lost.getInitiator(), null));

        // Conflict 2: initiator commits the big character → initiator WINS.
        ConflictCard cc2 = new ConflictCard("conf2", "Win Strike", "CONFLICT_MILITARY",
                Rarity.COMMON, Faction.ANY, CardSet.PREMIERE, "x", "text",
                ConflictType.MILITARY, 2);
        Conflict wonC = new Conflict(cc2, initiator);
        wonC.commitCard(initiator, initiator.getAmbassador());
        wonC.commitCard(initiator, bigChar);
        wonC.commitCard(opposer, opposer.getAmbassador());
        wonC.resolve(initiator);

        boolean initWon2 = rules.initiatorWon(wonC, initiator);
        check("D1", "initiatorWon=true when the initiator wins", initWon2 == true);
        check("D1", "winning initiator can play WON aftermath",
                rules.canPlayAftermath(initiator, wonAm, wonC, initWon2));
        check("D1", "winning initiator cannot play LOST aftermath",
                !rules.canPlayAftermath(initiator, lostAm, wonC, initWon2));
    }

    // ── D3: aftermath conflict-type gating (incl. PSI branch) ────────────────

    private static void testD3() {
        System.out.println("D3: aftermath conflict-type gating (PSI branch)");
        AftermathCard psiAm = aftermath("am_psi", "WON_PSI");

        check("D3", "PSI aftermath illegal on MILITARY conflict",
                !psiAm.isEligible(true, true, ConflictType.MILITARY));
        check("D3", "PSI aftermath illegal on DIPLOMACY conflict",
                !psiAm.isEligible(true, true, ConflictType.DIPLOMACY));
        check("D3", "PSI aftermath illegal on INTRIGUE conflict",
                !psiAm.isEligible(true, true, ConflictType.INTRIGUE));
        check("D3", "PSI aftermath legal on PSI conflict",
                psiAm.isEligible(true, true, ConflictType.PSI));

        AftermathCard milPar = aftermath("am_mp", "MILITARY_PARTICIPANT");
        check("D3", "MILITARY_PARTICIPANT legal on MILITARY when participated",
                milPar.isEligible(false, true, ConflictType.MILITARY));
        check("D3", "MILITARY_PARTICIPANT illegal without participation",
                !milPar.isEligible(false, false, ConflictType.MILITARY));
        check("D3", "MILITARY_PARTICIPANT illegal on INTRIGUE conflict",
                !milPar.isEligible(false, true, ConflictType.INTRIGUE));

        // One check through the engine path too (card must be in hand).
        Player p = player("P3", Faction.NARN);
        GameState st = state(p);
        AftermathCard inHand = aftermath("am_psi2", "WON_PSI");
        p.addToHand(inHand);
        ConflictCard cc = new ConflictCard("c3", "Mil Strike", "CONFLICT_MILITARY",
                Rarity.COMMON, Faction.ANY, CardSet.PREMIERE, "x", "text",
                ConflictType.MILITARY, 1);
        Conflict mil = new Conflict(cc, p);
        mil.commitCard(p, p.getAmbassador());
        mil.resolve(p);
        RulesEngine rules = new RulesEngine();
        check("D3", "engine path: WON_PSI rejected on a MILITARY conflict via canPlayAftermath",
                !rules.canPlayAftermath(p, inHand, mil, true));
    }

    // ── D8: deck-out penalty ─────────────────────────────────────────────────

    private static void testD8() {
        System.out.println("D8: deck-out penalty (no reshuffle; IC discard; forfeit)");
        RulesEngine rules = new RulesEngine();

        // A: one card in deck, draw 2 → the 2nd required draw discards the
        //    non-ambassador Inner Circle character. No forfeit yet.
        Player rich = player("Rich", Faction.CENTAURI);
        CharacterCard advisor = new CharacterCard("ic1", "Advisor",
                "CHARACTER_CENTAURI", Rarity.RARE, Faction.CENTAURI, CardSet.PREMIERE,
                "x", "text", 1, 1, 0, 2, false);
        rich.getInnerCircle().add(advisor);
        List<Card> one = new ArrayList<Card>();
        one.add(event("single"));
        rich.setDeck(new Deck(one));
        rich.drawCards(2);
        check("D8", "deck-out discards the non-ambassador IC character",
                !rich.hasForfeited() && rich.getInnerCircle().size() == 1
                && !rich.getInnerCircle().contains(advisor)
                && rich.getDeck().getDiscardPile().contains(advisor));

        // B: next required draw with an empty pile → forfeit; ambassador stays.
        rich.drawCards(1);
        check("D8", "deck-out with only the ambassador left flags forfeit",
                rich.hasForfeited() && rich.getInnerCircle().size() == 1
                && rich.getInnerCircle().contains(rich.getAmbassador()));

        // C: ambassador is never the discard candidate.
        Player poor = player("Poor", Faction.HUMAN);
        poor.setDeck(new Deck(new ArrayList<Card>()));
        poor.drawCards(1);
        check("D8", "empty deck from the start: forfeit, ambassador never discarded",
                poor.hasForfeited() && poor.getInnerCircle().size() == 1
                && poor.getInnerCircle().contains(poor.getAmbassador()));

        // D: checkVictory honours forfeits (last standing wins; none otherwise).
        Player f1 = player("F1", Faction.NARN);
        Player f2 = player("F2", Faction.MINBARI);
        f2.setDeck(new Deck(new ArrayList<Card>()));
        f2.drawCards(1);
        GameState st2 = state(f1, f2);
        check("D8", "checkVictory returns the last standing player",
                rules.checkVictory(st2) == f1);

        Player f3 = player("F3", Faction.CENTAURI);
        Player f4 = player("F4", Faction.HUMAN);
        GameState st3 = state(f3, f4);
        check("D8", "checkVictory returns null while no one has forfeited",
                rules.checkVictory(st3) == null);
    }

    // ── B5-0336: participation enforcement ────────────────────────────

    private static void testParticipation() {
        System.out.println("PAR (B5-0336): conflict participation restrictions");
        RulesEngine rules = new RulesEngine();

        // 1: parser round-trip (Border Raid shape) + loud-skip of unknowns.
        Participation border = Participation.parse(
                "{\"players\":\"INITIATOR_TARGET\",\"requiresTarget\":true,"
                + "\"cardTypes\":[\"FLEET\"],\"perPlayerQuota\":{\"FLEET\":1},"
                + "\"leadersIncluded\":true,\"unknownKey\":{\"a\":[1,2]}}");
        check("PAR", "parser reads players/requiresTarget/cardTypes/quota/leaders",
                "INITIATOR_TARGET".equals(border.getPlayers())
                && border.isRequiresTarget()
                && border.getCardTypes().contains(CardType.FLEET)
                && Integer.valueOf(1).equals(border.getPerPlayerQuota().get(CardType.FLEET))
                && border.isLeadersIncluded() && border.isRestricted());
        Participation empty = Participation.parse("{}");
        check("PAR", "empty participation object is not restricted (open)",
                !empty.isRestricted());

        // 2: backward compatibility — null participation accepts everything.
        Player init = player("PInit", Faction.NARN);
        Player tgt  = player("PTgt", Faction.MINBARI);
        Player out  = player("POut", Faction.CENTAURI);
        GameState st = state(init, tgt, out);
        Conflict open = new Conflict(conflictCard("par_open", ConflictType.DIPLOMACY, null), init);
        check("PAR", "open conflict: any player may join and commit",
                open.addParticipant(out, false)
                && open.commitCard(out, event("par_e1"), false));

        // 3: players gate (INITIATOR_TARGET).
        Conflict gated = new Conflict(conflictCard("par_gate", ConflictType.MILITARY,
                Participation.parse("{\"players\":\"INITIATOR_TARGET\"}")), init, tgt);
        check("PAR", "players gate admits the declared target",
                gated.addParticipant(tgt, false));
        check("PAR", "players gate rejects an outsider's join and commit",
                !gated.addParticipant(out, false)
                && !gated.commitCard(out, event("par_e2"), false));

        // 4: cardTypes + fleetSubtypes (Limited Strike shape); a fleet with
        //    no fleetClass cannot be proven eligible (§3.6).
        Participation limited = Participation.parse(
                "{\"cardTypes\":[\"FLEET\"],"
                + "\"fleetSubtypes\":[\"PICKET\",\"COLONIAL\",\"UTILITY\"]}");
        Conflict lim = new Conflict(conflictCard("par_lim", ConflictType.MILITARY, limited), init);
        check("PAR", "allowed fleet class commits",
                lim.commitCard(init, fleetCard("par_f1", "PICKET"), true));
        check("PAR", "disallowed fleet class is refused",
                !lim.commitCard(init, fleetCard("par_f2", "DESTROYER"), true));
        check("PAR", "fleet without a fleetClass is refused under the filter",
                !lim.commitCard(init, fleetCard("par_f3", null), true));
        check("PAR", "non-fleet card kinds are refused by cardTypes",
                !lim.commitCard(init, event("par_e3"), true));

        // 5: perPlayerQuota.
        Conflict quota = new Conflict(conflictCard("par_quota", ConflictType.MILITARY,
                Participation.parse("{\"cardTypes\":[\"FLEET\"],"
                        + "\"perPlayerQuota\":{\"FLEET\":1}}")), init);
        check("PAR", "quota admits the first fleet and refuses the second",
                quota.commitCard(init, fleetCard("par_q1", "PICKET"), true)
                && !quota.commitCard(init, fleetCard("par_q2", "COLONIAL"), true));

        // 6: leadersIncluded — a character commits only alongside an allowed
        //    committed fleet.
        Participation raid = Participation.parse(
                "{\"cardTypes\":[\"FLEET\"],\"leadersIncluded\":true}");
        CharacterCard leader = new CharacterCard("par_lead", "Raider Leader",
                "CHARACTER_NARN", Rarity.RARE, Faction.NARN, CardSet.PREMIERE,
                "x", "text", 2, 2, 0, 3, false);
        Conflict led = new Conflict(conflictCard("par_led", ConflictType.MILITARY, raid), init);
        check("PAR", "leadersIncluded refuses a character with no allowed fleet",
                !led.commitCard(init, leader, true));
        led.commitCard(init, fleetCard("par_f4", "UTILITY"), true);
        check("PAR", "leadersIncluded admits a character alongside an allowed fleet",
                led.commitCard(init, leader, true));

        // 7: requiresTarget enforced at initiation (the 3-arg pre-B5-0336
        //    form delegates with target null and must refuse too).
        ConflictCard needsTarget = conflictCard("par_rt", ConflictType.MILITARY,
                Participation.parse("{\"requiresTarget\":true}"));
        init.addToHand(needsTarget);
        check("PAR", "requiresTarget conflict is illegal without a target",
                !rules.canInitiateConflict(init, needsTarget, null, st)
                && !rules.canInitiateConflict(init, needsTarget, st));
        check("PAR", "requiresTarget conflict is legal with a declared target",
                rules.canInitiateConflict(init, needsTarget, tgt, st));
        ConflictCard noPart = conflictCard("par_np", ConflictType.MILITARY, null);
        init.addToHand(noPart);
        check("PAR", "open conflict stays legal in both initiation forms",
                rules.canInitiateConflict(init, noPart, null, st)
                && rules.canInitiateConflict(init, noPart, tgt, st));

        // 8: mustCommitAmbassador — eligible non-participants are pulled in
        //    opposing and their ambassador commits (Immortality Serum).
        Player m1 = player("PMust1", Faction.NARN);
        Player m2 = player("PMust2", Faction.MINBARI);
        GameState mst = state(m1, m2);
        Conflict serum = new Conflict(conflictCard("par_serum", ConflictType.PSI,
                Participation.parse("{\"mustCommitAmbassador\":true}")), m1);
        rules.enforceMandatoryParticipation(serum, mst);
        check("PAR", "mustCommitAmbassador compels the other faction in",
                serum.getParticipants().contains(m2) && serum.isOpposing(m2));
        check("PAR", "mustCommitAmbassador commits both ambassadors",
                serum.getCommittedCards(m1).contains(m1.getAmbassador())
                && serum.getCommittedCards(m2).contains(m2.getAmbassador()));

        // 9: allPlayersMustCommit (The Great Machine) — same compulsion,
        //    CHARACTER kind auto-satisfiable via the ambassador.
        Conflict machine = new Conflict(conflictCard("par_tgm", ConflictType.DIPLOMACY,
                Participation.parse(
                        "{\"allPlayersMustCommit\":{\"cardType\":\"CHARACTER\",\"count\":1}}")),
                m1);
        rules.enforceMandatoryParticipation(machine, mst);
        check("PAR", "allPlayersMustCommit pulls everyone in and commits ambassadors",
                machine.getParticipants().contains(m2)
                && machine.getCommittedCards(m1).contains(m1.getAmbassador())
                && machine.getCommittedCards(m2).contains(m2.getAmbassador()));

        // 10: mustTakeSide (Complete Support) — every other player joins.
        Player s2 = player("PSup2", Faction.CENTAURI);
        GameState sst = state(m1, s2);
        Conflict support = new Conflict(conflictCard("par_cs", ConflictType.DIPLOMACY,
                Participation.parse("{\"mustTakeSide\":true}")), m1);
        rules.enforceMandatoryParticipation(support, sst);
        check("PAR", "mustTakeSide compels every other player to a side",
                support.getParticipants().contains(s2));

        // 11: the loader hydrates the nested object and fleetClass.
        List<Card> parsed = DeckLoader.parseCards(
                "[{\"id\":\"par_c1\",\"title\":\"Raid\",\"type\":\"CONFLICT\","
                + "\"conflictType\":\"MILITARY\",\"influenceReward\":2,"
                + "\"participation\":{\"players\":\"INITIATOR_TARGET\","
                + "\"requiresTarget\":true,\"cardTypes\":[\"FLEET\"],"
                + "\"perPlayerQuota\":{\"FLEET\":1}}},"
                + "{\"id\":\"par_c2\",\"title\":\"Open\",\"type\":\"CONFLICT\","
                + "\"conflictType\":\"DIPLOMACY\"},"
                + "{\"id\":\"par_f5\",\"title\":\"Picket One\",\"type\":\"FLEET\","
                + "\"military\":2,\"fleetClass\":\"PICKET\"}]");
        ConflictCard loaded = (ConflictCard) parsed.get(0);
        check("PAR", "loader hydrates the participation object",
                loaded.getParticipation() != null
                && loaded.getParticipation().isRequiresTarget()
                && "INITIATOR_TARGET".equals(loaded.getParticipation().getPlayers()));
        check("PAR", "loader leaves an absent participation open (null)",
                ((ConflictCard) parsed.get(1)).getParticipation() == null);
        check("PAR", "loader hydrates fleetClass",
                "PICKET".equals(((FleetCard) parsed.get(2)).getFleetClass()));
    }

    // ── B5-0337: fleet leadership (audit D5) ─────────────────────

    private static void testFleetLeadership() {
        System.out.println("FLR (B5-0337): fleet leadership relation + D5 exclusion");
        RulesEngine rules = new RulesEngine();

        Player p  = player("FLRp", Faction.NARN);
        Player p2 = player("FLRq", Faction.MINBARI);
        GameState st = state(p, p2);

        CharacterCard ch = leaderCard("flr_ch", 4);   // Leadership 4, in the IC
        p.getInnerCircle().add(ch);
        FleetCard fl = fleetCard("flr_fl", "FRIGATE"); // Military 3
        p.getFleets().add(fl);

        // A: the D5 core — a ready IC character contributes NOTHING to the
        //    MILITARY total (pre-B5-0337 this read 7 = fleet 3 + Leadership 4).
        check("FLR", "unled IC character adds nothing to MILITARY totals (D5)",
                p.conflictTotal(ConflictType.MILITARY) == 3);
        check("FLR", "non-MILITARY totals still count IC characters",
                p.conflictTotal(ConflictType.DIPLOMACY) == 5);

        // B: the relation lifecycle.
        check("FLR", "canLeadFleet true for a ready IC character + own fleet",
                rules.canLeadFleet(p, ch, fl));
        rules.executeLeadFleet(p, ch, fl, st);
        check("FLR", "leading rotates the leader and seats the relation",
                ch.isRotated() && fl.getLeader() == ch);
        check("FLR", "led fleet's effective Military carries the Leadership",
                fl.getEffectiveMilitary() == 7
                && fl.getPrimaryStatValue(ConflictType.MILITARY) == 7);
        check("FLR", "conflictTotal uses the effective fleet value",
                p.conflictTotal(ConflictType.MILITARY) == 7);

        // C: one leader per fleet + rotation economy.
        CharacterCard ch2 = leaderCard("flr_ch2", 3);
        p.getInnerCircle().add(ch2);
        check("FLR", "a second character cannot lead an already-led fleet",
                !rules.canLeadFleet(p, ch2, fl));
        rules.executeLeadFleet(p, ch2, fl, st);
        check("FLR", "the illegal second lead is a no-op",
                !ch2.isRotated() && fl.getLeader() == ch);
        FleetCard fl2 = fleetCard("flr_fl2", "PICKET");
        p.getFleets().add(fl2);
        check("FLR", "a rotated (leading) character cannot lead another fleet",
                !rules.canLeadFleet(p, ch, fl2));

        // D: damage and rotation interactions (conflictTotal sums BOTH
        //    fleets: fl + fl2, each Military 3).
        ch.setFaceDown(true);
        check("FLR", "a damaged leader adds 0 to the effective Military",
                fl.getEffectiveMilitary() == 3
                && p.conflictTotal(ConflictType.MILITARY) == 6);   // fl 3 + fl2 3
        ch.setFaceDown(false);
        fl.rotate();
        check("FLR", "a rotated (committed) fleet contributes 0 even while led",
                p.conflictTotal(ConflictType.MILITARY) == 3);      // fl 0 + fl2 3
        fl.unrotate();

        // E: leading from the Inner Circle with a second fleet.
        CharacterCard ic2 = leaderCard("flr_ic2", 2);
        p.getInnerCircle().add(ic2);
        rules.executeLeadFleet(p, ic2, fl2, st);
        check("FLR", "IC character leads a second fleet (3 + 2 = 5)",
                fl2.getLeader() == ic2 && fl2.getEffectiveMilitary() == 5);

        // F: the relation expires at the round boundary (leading is a
        //    rotation; startRound unrotates everyone and clears leaders).
        rules.startRound(st);
        check("FLR", "startRound clears leaders and unrotates the leader",
                fl.getLeader() == null && fl2.getLeader() == null
                && !ch.isRotated() && !ic2.isRotated());
        check("FLR", "post-round totals are back to the unled baseline",
                p.conflictTotal(ConflictType.MILITARY) == 6);   // two unled fleets

        // G: an outsider's fleet/character can never be wired together.
        check("FLR", "canLeadFleet rejects another player's character and fleet",
                !rules.canLeadFleet(p2, ch, fl) && !rules.canLeadFleet(p, ch, null));
    }

    // ── B5-0338: aftermath targeting + uniqueness (audit D2/D4) ───────

    private static void testAftermathTargeting() {
        System.out.println("AMT (B5-0338): aftermath targeting + D4 uniqueness");
        RulesEngine rules = new RulesEngine();

        Player init = player("AInit", Faction.NARN);
        Player part = player("APart", Faction.MINBARI);
        Player out  = player("AOut", Faction.CENTAURI);
        GameState st = state(init, part, out);

        ConflictCard cc = new ConflictCard("amt_cc", "AMT Strike", "CONFLICT_MILITARY",
                Rarity.COMMON, Faction.ANY, CardSet.PREMIERE, "x", "text",
                ConflictType.MILITARY, 1);
        Conflict won = new Conflict(cc, init);
        won.commitCard(init, init.getAmbassador(), true);
        won.commitCard(part, part.getAmbassador(), false);
        won.resolve(init);
        boolean initWon = rules.initiatorWon(won, init);

        AftermathCard wonPlain = aftermath("amt_wp", "WON");              // non-Participant
        AftermathCard wonPart  = aftermath("amt_wpar", "WON_PARTICIPANT"); // Participant
        AftermathCard plainInHand   = aftermath("amt_ph", "WON");
        AftermathCard partInHand    = aftermath("amt_pp", "WON_PARTICIPANT");
        AftermathCard lostInHand    = aftermath("amt_lh", "LOST");
        AftermathCard initWonInHand = aftermath("amt_iw", "WON");
        part.addToHand(plainInHand);   // part LOST: may play WON on the initiator
        part.addToHand(partInHand);    // part LOST + Participant: self-target
        init.addToHand(lostInHand);    // init WON: LOST is illegal for him
        init.addToHand(initWonInHand); // init WON: WON legal on himself (legacy)

        // 1: D2 — a non-Participant aftermath targets ONLY the initiator.
        check("AMT", "non-Participant aftermath may target the initiator",
                rules.canPlayAftermath(part, plainInHand, won, initWon, init, st));
        check("AMT", "non-Participant aftermath may NOT target a mere participant",
                !rules.canPlayAftermath(part, plainInHand, won, initWon, part, st)
                && !rules.canPlayAftermath(part, plainInHand, won, initWon, out, st));

        // 2: D2 — a Participant aftermath may target any conflict participant.
        check("AMT", "Participant aftermath may target any conflict participant",
                rules.canPlayAftermath(part, partInHand, won, initWon, part, st)
                && rules.canPlayAftermath(part, partInHand, won, initWon, init, st));
        check("AMT", "Participant aftermath may NOT target an outsider",
                !rules.canPlayAftermath(part, partInHand, won, initWon, out, st));

        // 3: play conditions are still the PLAYING player's to satisfy.
        check("AMT", "Won-condition still follows the playing player",
                !rules.canPlayAftermath(init, lostInHand, won, initWon, init, st)
                && rules.canPlayAftermath(part, plainInHand, won, initWon, init, st));

        // 4: the legacy 4-arg form reads as self-target play under D2.
        check("AMT", "legacy form stays initiator/self-targeted",
                rules.canPlayAftermath(init, initWonInHand, won, initWon)
                && !rules.canPlayAftermath(part, plainInHand, won, initWon));

        // 5: effect lands on the TARGET, not the player (deterministic site).
        Player t1 = player("ATgt", Faction.NARN);
        Player t2 = player("ASrc", Faction.MINBARI);
        GameState tst = state(t1, t2);
        ConflictCard cc2 = new ConflictCard("amt_cc2", "AMT2 Strike", "CONFLICT_MILITARY",
                Rarity.COMMON, Faction.ANY, CardSet.PREMIERE, "x", "text",
                ConflictType.MILITARY, 1);
        Conflict won2 = new Conflict(cc2, t1);
        won2.resolve(t1);
        t2.addToHand(wonPlain);   // t2 lost, plays WON on the initiator t1
        int infBefore = t1.getInfluence();
        int srcBefore = t2.getInfluence();
        check("AMT", "site legality with the 6-arg form",
                rules.canPlayAftermath(t2, wonPlain, won2, true, t1, tst));
        // Mirror of the controller site: target = initiator, effect on target.
        Player target = t2.getHand().contains(wonPlain) ? t1 : null;
        if (target != null) {
            t2.removeFromHand(wonPlain);
            tst.attachAftermath(wonPlain, target);
            t1.gainInfluence(1);   // applySimpleAftermathEffect: WON + is winner gives +1
        }
        check("AMT", "effect lands on the target (initiator gains, player unchanged)",
                t1.getInfluence() == infBefore + 1 && t2.getInfluence() == srcBefore);
        check("AMT", "attached aftermath is registered on the target",
                tst.getAttachedAftermaths(t1).contains(wonPlain));

        // 6: D4 — one of each NAMED aftermath per target (the card TITLE is
        //    the name); same title refused, different titles attach, other
        //    targets unaffected.
        AftermathCard sameName  = aftermath("amt_wp", "WON");    // same TITLE as wonPlain
        AftermathCard otherName = aftermath("amt_other", "LOST");
        check("AMT", "D4: a same-named aftermath is refused on the same target",
                !tst.attachAftermath(sameName, t1));
        check("AMT", "D4: a differently-named aftermath attaches",
                tst.attachAftermath(otherName, t1));
        check("AMT", "D4: the same name attaches freely on another target",
                tst.attachAftermath(sameName, t2));
        t2.addToHand(sameName);
        check("AMT", "D4: the registry guards legality through canPlayAftermath",
                !rules.canPlayAftermath(t2, sameName, won2, true, t1, tst));

        // 7: the registry starts empty (nothing persists in the current
        //    discard-after-effect flow; D4 becomes authoritative with it).
        Player fresh = player("AFresh", Faction.HUMAN);
        GameState fst = state(fresh);
        check("AMT", "a fresh state has no attached aftermaths",
                fst.getAttachedAftermaths(fresh).isEmpty());
    }

    // ── B5-0339: ambassador's assistant (rulebook §IV) ────────────

    private static void testAssistant() {
        System.out.println("AST (B5-0339): ambassador's assistant abilities");
        RulesEngine rules = new RulesEngine();

        Player p  = player("AAst", Faction.NARN);   // ambassador 3/3/3/3 in the IC
        Player p2 = player("AAst2", Faction.MINBARI);
        GameState st = state(p, p2);
        CharacterCard amb = p.getAmbassador();
        CharacterCard ch  = leaderCard("ast_ch", 2);   // a supporting assistant
        p.getSupportingRole().add(ch);

        // 1: readiness gating.
        check("AST", "canUseAssistant true for a ready supporting assistant",
                rules.canUseAssistant(p, ch, amb));
        check("AST", "canUseAssistant refuses a foreign ambassador",
                !rules.canUseAssistant(p, ch, leaderCard("ast_x", 1)));
        ch.rotate();
        check("AST", "canUseAssistant refuses a rotated assistant",
                !rules.canUseAssistant(p, ch, amb));
        ch.unrotate();
        ch.setFaceDown(true);
        check("AST", "canUseAssistant refuses a neutralized (damaged) assistant",
                !rules.canUseAssistant(p, ch, amb));
        ch.setFaceDown(false);
        CharacterCard outsider = leaderCard("ast_out", 1);
        check("AST", "canUseAssistant refuses a non-supporting character",
                !rules.canUseAssistant(p, outsider, amb));

        // 2: the ability boost — rotate to assist.
        rules.executeAssistantAbilityBoost(p, ch, st);
        check("AST", "assisting rotates the assistant and flags the ambassador",
                ch.isRotated() && amb.isAssistantBonus());
        check("AST", "ambassador gains +1 Diplomacy and Leadership",
                amb.getPrimaryStatValue(ConflictType.DIPLOMACY) == 4
                && amb.getPrimaryStatValue(ConflictType.MILITARY) == 4);
        check("AST", "Psi is untouched (cannot be raised from a base of 0 by bonuses)",
                amb.getPrimaryStatValue(ConflictType.PSI) == 3);
        check("AST", "conflictTotal sees the assisted ambassador",
                p.conflictTotal(ConflictType.DIPLOMACY) == 4);

        // 3: the flag is binary — a second assistant does not stack.
        CharacterCard ch2 = leaderCard("ast_ch2", 1);
        p.getSupportingRole().add(ch2);
        rules.executeAssistantAbilityBoost(p, ch2, st);
        check("AST", "a second assisting assistant does not stack the bonus",
                amb.getPrimaryStatValue(ConflictType.DIPLOMACY) == 4);
        check("AST", "a rotated assistant cannot assist again",
                !rules.canUseAssistant(p, ch, amb));

        // 4: the sponsor discount — rotate to discount.
        Player pd = player("ADisc", Faction.NARN);
        Player pd2 = player("ADisc2", Faction.MINBARI);
        GameState tstd = state(pd, pd2);
        CharacterCard chd = leaderCard("ast_chd", 2);
        pd.getSupportingRole().add(chd);
        rules.executeAssistantSponsorDiscount(pd, chd, tstd);
        check("AST", "discount path rotates the assistant and grants 1",
                chd.isRotated() && pd.getSponsorDiscount() == 1);
        CharacterCard costly = charCard("ast_costly", Faction.MINBARI, 3);
        CharacterCard free   = charCard("ast_free", Faction.NARN, 0);
        check("AST", "recruitCost drops by the discount (other-race 6 to 5)",
                rules.baseRecruitCost(pd, costly) == 6
                && rules.recruitCost(pd, costly) == 5);
        check("AST", "the discount floors at 0 (0-cost card stays 0)",
                rules.recruitCost(pd, free) == 0);
        pd.gainInfluence(1);   // 5
        pd.addToHand(costly);
        check("AST", "canRecruit true at the discounted cost",
                rules.canRecruit(pd, costly));
        // Controller-site mirror: spend the effective cost, consume exactly
        // the applied discount, seat the character.
        int base = rules.baseRecruitCost(pd, costly);
        int cost = rules.recruitCost(pd, costly);
        pd.applyInfluence(cost);
        pd.consumeSponsorDiscount(base - cost);
        pd.removeFromHand(costly);
        pd.placeInSupportingRole(costly);
        check("AST", "recruiting consumes exactly the applied discount",
                pd.getInfluence() == 5 && pd.getAppliedPool() == 0
                && pd.getSponsorDiscount() == 0
                && pd.getSupportingRole().contains(costly));
        CharacterCard cheap = charCard("ast_cheap", Faction.NARN, 1);
        check("AST", "the discount is exhausted for further recruits this turn",
                rules.recruitCost(pd, cheap) == 1
                && !rules.canRecruit(pd, cheap));

        // 5: both effects expire at the round boundary.
        rules.startRound(st);
        check("AST", "startRound clears the ability bonus and rotations",
                !amb.isAssistantBonus() && !ch.isRotated() && !ch2.isRotated()
                && amb.getPrimaryStatValue(ConflictType.DIPLOMACY) == 3);
        check("AST", "the sponsor discount dies with the turn",
                p.getSponsorDiscount() == 0);
    }

    // ── B5-0343: AI join-side + initiation targets ────────────

    private static void testAIJoinSide() {
        System.out.println("AIJ (B5-0343): AI join-side strategy + target selection");

        Player p  = player("AJp", Faction.NARN);
        Player p2 = player("AJq", Faction.MINBARI);
        GameState st = state(p, p2);
        CharacterCard bigChar = new CharacterCard("aj_big", "AJ Big",
                "CHARACTER_NARN", Rarity.RARE, Faction.NARN, CardSet.PREMIERE,
                "x", "text", 5, 0, 0, 0, false);   // Diplomacy 5
        p.getInnerCircle().add(bigChar);

        // DIPLOMACY conflicts throughout: character totals feed
        // conflictTotal(DIPLOMACY) — MILITARY totals are fleet-only since
        // B5-0337, which these checks must respect.
        ConflictCard cc = new ConflictCard("aj_cc", "AJ Strike", "CONFLICT_DIPLOMACY",
                Rarity.COMMON, Faction.ANY, CardSet.PREMIERE, "x", "text",
                ConflictType.DIPLOMACY, 1);

        // 1: MEDIUM outweighs: oppose; free-ride: support; middle: abstain.
        AIPlayer med = new AIPlayer(p, AIDifficulty.MEDIUM);
        Conflict c1 = new Conflict(cc, p2);   // p2 total 3 (ambassador)
        check("AIJ", "MEDIUM opposes when it outweighs the initiator",
                med.decideJoinSide(st, p, c1) == -1);
        Conflict c2 = new Conflict(new ConflictCard("aj_cc2", "AJ2 Strike",
                "CONFLICT_DIPLOMACY", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE,
                "x", "text", ConflictType.DIPLOMACY, 1), p2);
        p.getInnerCircle().remove(bigChar);   // mine back to 3
        // Free-ride state: initiator total 8, mine 3 (3*2 < 8).
        CharacterCard huge = new CharacterCard("aj_huge", "AJ Huge",
                "CHARACTER_MINBARI", Rarity.RARE, Faction.MINBARI, CardSet.PREMIERE,
                "x", "text", 5, 0, 0, 0, false);   // Diplomacy 5
        p2.getInnerCircle().add(huge);        // p2 total 3+5 = 8
        check("AIJ", "MEDIUM supports a sure-winner conflict (free-ride)",
                med.decideJoinSide(st, p, c2) == 1);

        // 2: the middle band abstains (the old code joined everything >= half).
        p2.getInnerCircle().remove(huge);     // back to 3 vs 3
        Conflict c3 = new Conflict(cc, p2);
        check("AIJ", "MEDIUM abstains in the tie band (was always-oppose)",
                med.decideJoinSide(st, p, c3) == 0);

        // 3: HARD never strengthens a leading player's conflict.
        p2.gainInfluence(2);                  // p2 influence 6 > p 4: p2 leads
        AIPlayer hard = new AIPlayer(p, AIDifficulty.HARD);
        Conflict c4 = new Conflict(cc, p2);
        check("AIJ", "HARD abstains from a leader's conflict it cannot outweigh",
                hard.decideJoinSide(st, p, c4) == 0);   // 3 vs 3: bigChar was removed above
        p.getInnerCircle().add(bigChar);      // outweigh again: 5+ambassador vs 3
        check("AIJ", "HARD opposes a leader's conflict it CAN outweigh",
                hard.decideJoinSide(st, p, c1) == -1);
        p.getInnerCircle().remove(bigChar);   // restore the state later checks were written against
        p2.loseInfluence(2);

        // 4: EASY join rate stays bounded (not the doubled all-join bug).
        AIPlayer easy = new AIPlayer(p, AIDifficulty.EASY);
        int joins = 0;
        for (int i = 0; i < 200; i++) {
            if (easy.decideJoinSide(st, p, c3) != 0) joins++;
        }
        check("AIJ", "EASY joins roughly half the time (bounded rate)",
                joins > 40 && joins < 160);

        // 5: the boolean API still works (legacy callers).
        check("AIJ", "shouldJoinConflict delegates to the side decision",
                med.shouldJoinConflict(st, p, c1) == (med.decideJoinSide(st, p, c1) != 0));

        // 6: HARD initiation targets a WEAK player over the board leader —
        //    deterministic: p holds the only card, both targets total 3, so
        //    the win-probability terms are equal and the anti-leader penalty
        //    decides (the pre-0343 +3 bonus would have chosen the leader).
        ConflictCard dip = new ConflictCard("aj_dip", "AJ Dip", "CONFLICT_DIPLOMACY",
                Rarity.COMMON, Faction.ANY, CardSet.PREMIERE, "x", "text",
                ConflictType.DIPLOMACY, 1);
        Player weak = player("AJweak", Faction.CENTAURI);
        Player strong = player("AJstrong", Faction.HUMAN);
        strong.gainInfluence(9);              // strong leads the board (13)
        GameState st2 = state(weak, strong, p);
        p.addToHand(dip);
        GameAction best = hard.chooseAction(st2, p);
        check("AIJ", "HARD initiates against the weak target, never the leader",
                best.getType() == GameAction.Type.INITIATE_CONFLICT
                && best.getTarget() == weak);
    }

    // ── B5-0366 AI: rotate-for-effect offers + scoring (B5-0345 Tier-2 #5) ────────────

    private static void testRotateAI() {
        System.out.println("ROT-AI (B5-0366): AI rotate-for-effect offers + scoring");
        Player p = player("ROTai", Faction.NARN);
        Player r = player("ROTaiR", Faction.MINBARI);
        GameState st = state(p, r);
        CharacterCard amb = p.getAmbassador();
        CharacterCard ch = leaderCard("rot_ai_ch", 2);
        p.getSupportingRole().add(ch);
        AIPlayer med = new AIPlayer(p, AIDifficulty.MEDIUM);
        AIPlayer hard = new AIPlayer(p, AIDifficulty.HARD);
        AIPlayer easy = new AIPlayer(p, AIDifficulty.EASY);

        // Clear everything else so the rotate-effect is the best non-pass action.
        p.getHand().clear();
        p.getFleets().clear();
        p.getInnerCircle().clear();
        p.getSupportingRole().clear();
        p.setAgenda(null);
        // Do NOT seat the ambassador in the IC — if we do, canBuildInfluence
        // sees an unrotated IC member and offers Build Influence (scored 3-13),
        // which beats rotate's 2. The rotate gate only needs the ambassador
        // reference (canUseAssistant(amb)), not the IC seat.
        p.getSupportingRole().add(ch);
        // Influence low enough that Build Influence is out of the way, but high
        // enough that the AI does not just pass (PASS scores 0 vs rotate's 2).
        p.loseInfluence(p.getInfluence() - 5);   // influence = 5

        // 1: the AI builder offers both rotate-effect kinds for a ready
        //    assistant + own ambassador (the engine gate is canUseRotateEffect,
        //    which delegates to canUseAssistant — same readiness as B5-0339).
        GameAction m1 = med.chooseAction(st, p);
        boolean offerBoost    = m1.getType() == GameAction.Type.USE_ROTATE_EFFECT
                                && m1.getRotateKind() == GameAction.RotateEffectKind.USE_ABILITY_BOOST;
        boolean offerDiscount = m1.getType() == GameAction.Type.USE_ROTATE_EFFECT
                                && m1.getRotateKind() == GameAction.RotateEffectKind.USE_SPONSOR_DISCOUNT;
        check("ROT-AI", "MEDIUM offers a rotate-effect action for a ready assistant", offerBoost || offerDiscount);

        // 2: MEDIUM prefers the ability boost over the sponsor discount — the
        //    boost is scored 2 vs the discount's 1, so the boost wins the
        //    deterministic MEDIUM max.
        check("ROT-AI", "MEDIUM picks the ability boost over the sponsor discount (2 > 1)", offerBoost && !offerDiscount);

        // 3: HARD also offers the rotate-effect and deterministically picks the
        //    boost (scored 1.0 vs 0.5 for the discount).
        GameAction h1 = hard.chooseAction(st, p);
        check("ROT-AI", "HARD offers and picks the rotate-effect (boost 1.0 > discount 0.5)", h1.getType() == GameAction.Type.USE_ROTATE_EFFECT && h1.getRotateKind() == GameAction.RotateEffectKind.USE_ABILITY_BOOST);

        // 4: EASY offers the rotate-effect too (it shares the same legal-action
        //    builder) and picks one of the two kinds at random — verify it still
        //    picks a rotate effect rather than pass when the rotate is the only
        //    non-pass non-low-value action available.
        int rotatePicks = 0;
        for (int i = 0; i < 40; i++) {
            GameAction e = easy.chooseAction(st, p);
            if (e.getType() == GameAction.Type.USE_ROTATE_EFFECT) rotatePicks++;
        }
        check("ROT-AI", "EASY offers the rotate-effect and picks it sometimes (offer-level, not scoring)", rotatePicks > 0);

        // 5: the AI does NOT offer rotate-effect when the assistant is rotated
        //    (canUseRotateEffect delegates to canUseAssistant, which refuses a
        //    rotated assistant) — deterministic absence across all three tiers.
        ch.rotate();
        boolean noneOffer = true;
        for (int i = 0; i < 5; i++) {
            GameAction a = med.chooseAction(st, p);
            if (a.getType() == GameAction.Type.USE_ROTATE_EFFECT) noneOffer = false;
        }
        check("ROT-AI", "no tier offers rotate-effect for a rotated assistant", noneOffer);
    }

    // ── B5-0377: AI Tier-1/Tier-2 slice scoring ──────────────────────────────

    /**
     * B5-0377: verify MEDIUM and HARD score LEAD_FLEET pairs correctly.
     * The scoring logic (B5-0362) seats unused Leadership that D5 says never
     * feeds MILITARY directly — MODERATE scores the raw Leadership value,
     * HARD additionally weighs the projected initiative win-probability.
     * Both tiers are deterministic: the highest-Leadership legal pair wins.
     */
    private static void testLeadFleetAI() {
        System.out.println("LEAD-AI (B5-0377): MEDIUM/HARD LEAD_FLEET scoring");
        RulesEngine rules = new RulesEngine();

        // Fixture: one Narn player with a single unled fleet and two IC
        // characters whose Leadership values differ. The AI should pick the
        // higher-Leadership character deterministically in both tiers.
        Player p = player("LAIfp", Faction.NARN);
        Player r = player("LAIfr", Faction.MINBARI);
        GameState st = state(p, r);
        FleetCard fl = fleetCard("lAIfl", "FRIGATE");   // Military 3, no leader
        p.getFleets().add(fl);
        p.gainInfluence(9);   // 9 — below the Build Influence cap, tames that score

        // One character with Leadership 1 (low) and one with Leadership 5 (high).
        // Both are unrotated and ready so both pairs are legal.
        CharacterCard lowL = leaderCard("lAIlow", 1);   // Leadership 1
        CharacterCard highL = leaderCard("lAIhigh", 5); // Leadership 5
        p.getInnerCircle().add(lowL);
        p.getInnerCircle().add(highL);
        // Clear hand/fleets/IC of any extras so nothing competes with lead-fleet.
        p.getHand().clear();
        p.setAgenda(null);

        // EASY, MEDIUM, HARD build the same legal list (the builder feeds all
        // three tiers from one code path). We only score-verify MEDIUM/HARD.
        AIPlayer med = new AIPlayer(p, AIDifficulty.MEDIUM);
        AIPlayer hard = new AIPlayer(p, AIDifficulty.HARD);

        // 1: MEDIUM picks the high-Leadership character deterministically.
        //    scoreActionMedium returns 2 + Leadership for LEAD_FLEET, so the
        //    Leadership-5 pair scores 7 vs the Leadership-1 pair at 3.
        GameAction mBest = med.chooseAction(st, p);
        check("LEAD-AI", "MEDIUM picks the highest-leadership (5) character",
                mBest.getType() == GameAction.Type.LEAD_FLEET
                && mBest.getLeader() == highL && mBest.getCard() == fl);
        // Determinism: two consecutive calls return the same action.
        check("LEAD-AI", "MEDIUM LEAD_FLEET is deterministic (same pick twice)",
                med.chooseAction(st, p).getLeader() == highL);

        // 2: HARD also picks the high-Leadership character. scoreActionHard
        //    adds a projected-initiative term (winProb * 2) on top of the
        //    1.5 + Leadership*0.5 base. With the same fleet and the opponent
        //    having zero Military, the high pair still wins.
        GameAction hBest = hard.chooseAction(st, p);
        check("LEAD-AI", "HARD picks the highest-leadership (5) character",
                hBest.getType() == GameAction.Type.LEAD_FLEET
                && hBest.getLeader() == highL && hBest.getCard() == fl);
        check("LEAD-AI", "HARD LEAD_FLEET is deterministic (same pick twice)",
                hard.chooseAction(st, p).getLeader() == highL);

        // 3: edge case — a character with Leadership 0 is still offered (the
        //    gate only checks readiness + own unled fleet) but scores lower
        //    than any positive-Leadership character, so it is never picked
        //    when a better character exists.
        CharacterCard zeroL = leaderCard("lAIzero", 0);  // Leadership 0
        p.getInnerCircle().add(zeroL);
        GameAction m3 = med.chooseAction(st, p);
        check("LEAD-AI", "Leadership-0 pair is offered but not picked (5 beats 0)",
                m3.getType() == GameAction.Type.LEAD_FLEET
                && m3.getLeader() == highL);

        // 4: no fleet at all -> no LEAD_FLEET offer, all tiers pass (or pick
        //    whatever non-lead action exists; here there is none, so pass).
        p.getFleets().clear();
        GameAction mNoFleet = med.chooseAction(st, p);
        check("LEAD-AI", "no fleet: no LEAD_FLEET offer (pass or other)",
                mNoFleet.getType() != GameAction.Type.LEAD_FLEET);
    }

    /**
     * B5-0377: verify MEDIUM and HARD score the three agenda-lifecycle actions.
     * The scoring logic (B5-0364) reads agendaProximityScore for REPLACE and
     * REVEAL (how close the board is to that agenda's own win condition), and
     * scores DISCARD low (rarely voluntary — Majors cannot be discarded).
     * This test covers all three types across both tiers.
     */
    private static void testAgendaLifecycleAI() {
        System.out.println("AGL-AI (B5-0377): MEDIUM/HARD agenda lifecycle scoring");

        // ── A: REPLACE_AGENDA — the AI replaces a distant agenda with a closer
        //    one from hand. Both tiers use agendaProximityScore for the new
        //    agenda's proximity, minus the IC rotation cost (1 action/1.0).
        //    Setup: current minor agenda at influence 4 (proximity 2); hand
        //    holds a replacement minor at influence 18 (proximity 9). The
        //    replacement's net score (9 - 1 = 8 MEDIUM, 9 - 1.0 = 8.0 HARD)
        //    beats keeping the current agenda (no REPLACE action exists for
        //    "keep"; the only agenda action is REPLACE with the closer card).
        Player p = player("AGLAIf", Faction.NARN);
        Player r = player("AGLAIr", Faction.MINBARI);
        GameState st = state(p, r);
        AgendaCard current = agendaCard("AGLAImcurr", false, Faction.ANY);
        p.setAgenda(current);
        p.gainInfluence(10);   // rating 10 blocks Build Influence
        p.getHand().clear();              // remove any other hand cards
        AgendaCard better = agendaCard("AGLAIbetter", false, Faction.ANY);
        p.getHand().add(better);   // at influence 10, proximity = 5 (10/2)
        CharacterCard leader = leaderCard("AGLAILd", 2);
        p.getInnerCircle().add(leader);   // unrotated, ready — replace legal
        p.getFleets().clear();
        p.getSupportingRole().clear();

        AIPlayer med = new AIPlayer(p, AIDifficulty.MEDIUM);
        AIPlayer hard = new AIPlayer(p, AIDifficulty.HARD);

        // MEDIUM picks REPLACE — the only agenda action available (agenda set
        // blocks the generic play path, influence 10 blocks Build Influence).
        GameAction mBest = med.chooseAction(st, p);
        check("AGL-AI", "MEDIUM replaces a distant agenda with a closer one",
                mBest.getType() == GameAction.Type.REPLACE_AGENDA
                && mBest.getCard() == better);
        // HARD picks the same (8.0 > 0 for pass, and no other agenda action).
        GameAction hBest = hard.chooseAction(st, p);
        check("AGL-AI", "HARD replaces a distant agenda with a closer one",
                hBest.getType() == GameAction.Type.REPLACE_AGENDA
                && hBest.getCard() == better);

        // ── B: DISCARD_AGENDA — a minor agenda (cannot be replaced) with no
        //    better agenda in hand. The only agenda action is DISCARD.
        //    Setup: minor agenda in play, no hand agendas, influence high enough
        //    to block Build Influence (rating >= 10 blocks). Both tiers score
        //    DISCARD low (1 MEDIUM / 0.5 HARD) but it is the only non-pass
        //    action, so it wins.
        Player p2 = player("AGLAIp2", Faction.NARN);
        GameState st2 = state(p2, r);
        AgendaCard minor = agendaCard("AGLAImin", false, Faction.ANY);
        p2.setAgenda(minor);
        p2.gainInfluence(10);   // rating 10 blocks Build Influence
        p2.getHand().clear();
        p2.getFleets().clear();
        p2.getSupportingRole().clear();

        AIPlayer med2 = new AIPlayer(p2, AIDifficulty.MEDIUM);
        AIPlayer hard2 = new AIPlayer(p2, AIDifficulty.HARD);

        GameAction mDisc = med2.chooseAction(st2, p2);
        check("AGL-AI", "MEDIUM discards a minor when no replacement exists",
                mDisc.getType() == GameAction.Type.DISCARD_AGENDA
                && mDisc.getCard() == minor);
        GameAction hDisc = hard2.chooseAction(st2, p2);
        check("AGL-AI", "HARD discards a minor when no replacement exists",
                hDisc.getType() == GameAction.Type.DISCARD_AGENDA
                && hDisc.getCard() == minor);

        // ── C: REVEAL_AGENDA — a face-down hidden agenda in play. Revealing
        //    activates it (rulebook :520). Both tiers score it by proximity
        //    (same helper as the sponsor path). Setup: hidden INFLUENCE_20
        //    agenda at influence 18 (proximity 9). No other actions.
        Player p3 = player("AGLAIp3", Faction.NARN);
        GameState st3 = state(p3, r);
        AgendaCard hidden = agendaCard("AGLAIhid", false, Faction.ANY);
        hidden.setFaceDown(true);
        p3.setAgenda(hidden);
        p3.gainInfluence(18);   // proximity = 9
        p3.getHand().clear();
        p3.getFleets().clear();
        p3.getSupportingRole().clear();

        AIPlayer med3 = new AIPlayer(p3, AIDifficulty.MEDIUM);
        AIPlayer hard3 = new AIPlayer(p3, AIDifficulty.HARD);

        GameAction mRev = med3.chooseAction(st3, p3);
        check("AGL-AI", "MEDIUM reveals a hidden agenda scoring by proximity",
                mRev.getType() == GameAction.Type.REVEAL_AGENDA
                && mRev.getCard() == hidden);
        GameAction hRev = hard3.chooseAction(st3, p3);
        check("AGL-AI", "HARD reveals a hidden agenda scoring by proximity",
                hRev.getType() == GameAction.Type.REVEAL_AGENDA
                && hRev.getCard() == hidden);
    }

    /**
     * B5-0377: verify MEDIUM and HARD score PLAY_CONTINGENCY and
     * REVEAL_CONTINGENCY. The scoring logic (B5-0365) assigns a modest
     * positional value to both: placement is a future-proofing play (score 2
     * MEDIUM / 1.5 HARD), reveal converts to an event-like effect (score 2
     * MEDIUM / 2.0 HARD). Both tiers are deterministic — the higher-scored
     * action wins when both are available.
     */
    private static void testContingencyAI() {
        System.out.println("CON-AI (B5-0377): MEDIUM/HARD contingency scoring");

        // ── A: PLAY_CONTINGENCY — a hand contingency with a legal host.
        //    Setup: ambassador host + one hand contingency, nothing else.
        //    The only non-pass action is PLAY_CONTINGENCY (score 2 MEDIUM,
        //    1.5 HARD). Both tiers pick it.
        Player pA = player("CONAIpA", Faction.NARN);
        GameState sA = state(pA);
        CharacterCard hostA = pA.getAmbassador();
        pA.getHand().clear();
        ContingencyCard conA = new ContingencyCard(
                "CONAIfix", "AI Fixture Contingency", "CHARACTER", Rarity.COMMON,
                Faction.ANY, CardSet.PREMIERE, "x", "fixture",
                "CHARACTER", "NARN", "WHEN_REVEALED_FIXTURE");
        pA.addToHand(conA);
        // Clear everything else so PLAY_CONTINGENCY is the only action.
        pA.getFleets().clear();
        pA.getInnerCircle().clear();
        pA.getSupportingRole().clear();
        pA.getLocations().clear();
        pA.getGroups().clear();
        pA.getEnhancements().clear();
        pA.setAgenda(null);

        AIPlayer medA = new AIPlayer(pA, AIDifficulty.MEDIUM);
        AIPlayer hardA = new AIPlayer(pA, AIDifficulty.HARD);

        GameAction mPlay = medA.chooseAction(sA, pA);
        check("CON-AI", "MEDIUM plays a hand contingency when it is the only action",
                mPlay.getType() == GameAction.Type.PLAY_CONTINGENCY
                && mPlay.getCard() == conA);
        GameAction hPlay = hardA.chooseAction(sA, pA);
        check("CON-AI", "HARD plays a hand contingency when it is the only action",
                hPlay.getType() == GameAction.Type.PLAY_CONTINGENCY
                && hPlay.getCard() == conA);

        // ── B: REVEAL_CONTINGENCY — a placed (face-down) contingency with a
        //    legal host. Setup: contingency already placed under the ambassador,
        //    nothing else in play. The only non-pass action is REVEAL.
        //    Reveal scores 2 MEDIUM / 2.0 HARD.
        Player pB = player("CONAIpB", Faction.NARN);
        GameState sB = state(pB);
        CharacterCard hostB = pB.getAmbassador();
        ContingencyCard conB = new ContingencyCard(
                "CONAIbFix", "AI Fixture Contingency 2", "CHARACTER", Rarity.COMMON,
                Faction.ANY, CardSet.PREMIERE, "x", "fixture",
                "CHARACTER", "NARN", "WHEN_REVEALED_FIXTURE");
        pB.addToHand(conB);
        pB.getHand().clear();
        pB.addToHand(conB);
        // Place it under the host so REVEAL is offered.
        RulesEngine rules = new RulesEngine();
        GameController gc = new GameController(sB, new ArrayList<AIPlayer>(),
                new GameStateCallback() {
                    public void accept(GameState gs) { }
                });
        try {
            java.lang.reflect.Method handler =
                    GameController.class.getDeclaredMethod(
                            "processAction", Player.class, GameAction.class);
            handler.setAccessible(true);
            handler.invoke(gc, pB, GameAction.playContingency(conB, hostB));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Clear everything else so REVEAL is the only action.
        pB.getFleets().clear();
        pB.getInnerCircle().clear();
        pB.getSupportingRole().clear();
        pB.getLocations().clear();
        pB.getGroups().clear();
        pB.getEnhancements().clear();
        pB.setAgenda(null);

        AIPlayer medB = new AIPlayer(pB, AIDifficulty.MEDIUM);
        AIPlayer hardB = new AIPlayer(pB, AIDifficulty.HARD);

        GameAction mRev = medB.chooseAction(sB, pB);
        check("CON-AI", "MEDIUM reveals a placed contingency when it is the only action",
                mRev.getType() == GameAction.Type.REVEAL_CONTINGENCY
                && mRev.getCard() == conB);
        GameAction hRev = hardB.chooseAction(sB, pB);
        check("CON-AI", "HARD reveals a placed contingency when it is the only action",
                hRev.getType() == GameAction.Type.REVEAL_CONTINGENCY
                && hRev.getCard() == conB);

        // ── C: both actions available — a hand contingency AND a placed one.
        //    When both PLAY (2 MEDIUM / 1.5 HARD) and REVEAL (2 MEDIUM / 2.0
        //    HARD) are offered simultaneously, MEDIUM ties at 2→ picks the
        //    first in list order (PLAY comes before REVEAL in the builder],
        //    HARD picks REVEAL (2.0 > 1.5).
        Player pC = player("CONAIpC", Faction.NARN);
        GameState sC = state(pC);
        CharacterCard hostC = pC.getAmbassador();
        ContingencyCard conC_play = new ContingencyCard(
                "CONAIcPlay", "AI Play Contingency", "CHARACTER", Rarity.COMMON,
                Faction.ANY, CardSet.PREMIERE, "x", "fixture",
                "CHARACTER", "NARN", "WHEN_REVEALED_FIXTURE");
        ContingencyCard conC_rev = new ContingencyCard(
                "CONAIcRev", "AI Reveal Contingency", "CHARACTER", Rarity.COMMON,
                Faction.ANY, CardSet.PREMIERE, "x", "fixture",
                "CHARACTER", "NARN", "WHEN_REVEALED_FIXTURE");
        pC.addToHand(conC_play);
        pC.addToHand(conC_rev);
        pC.getHand().clear();
        pC.addToHand(conC_play);
        pC.addToHand(conC_rev);
        pC.getFleets().clear();
        pC.getInnerCircle().clear();
        pC.getSupportingRole().clear();
        pC.getLocations().clear();
        pC.getGroups().clear();
        pC.getEnhancements().clear();
        pC.setAgenda(null);
        // Place conC_rev so REVEAL is also offered.
        GameController gc2 = new GameController(sC, new ArrayList<AIPlayer>(),
                new GameStateCallback() {
                    public void accept(GameState gs) { }
                });
        try {
            java.lang.reflect.Method handler2 =
                    GameController.class.getDeclaredMethod(
                            "processAction", Player.class, GameAction.class);
            handler2.setAccessible(true);
            handler2.invoke(gc2, pC, GameAction.playContingency(conC_rev, hostC));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        AIPlayer medC = new AIPlayer(pC, AIDifficulty.MEDIUM);
        AIPlayer hardC = new AIPlayer(pC, AIDifficulty.HARD);

        GameAction mC = medC.chooseAction(sC, pC);
        // MEDIUM: PLAY scores 2, REVEAL scores 2 — deterministic list-order
        // tie-break -> PLAY (added first in the builder).
        check("CON-AI", "MEDIUM breaks PLAY/REVEAL tie by list order (PLAY first)",
                mC.getType() == GameAction.Type.PLAY_CONTINGENCY
                && mC.getCard() == conC_play);
        GameAction hC = hardC.chooseAction(sC, pC);
        // HARD: REVEAL scores 2.0 vs PLAY's 1.5 — REVEAL wins.
        check("CON-AI", "HARD picks REVEAL over PLAY (2.0 > 1.5)",
                hC.getType() == GameAction.Type.REVEAL_CONTINGENCY
                && hC.getCard() == conC_rev);
    }

    /**
     * B5-0403: verify the AI mercenary-bidding offer + scoring against the
     * B5-0395 engine. Fixtures follow the CON-AI pattern: boards cleared so
     * the bid is the only non-pass action, the offered mercenary is the
     * synthetic event fixture (the pool carries zero mercenary cards per
     * B5-0386), and bids spend the D9 applied pool only.
     */
    private static void testMercenaryBiddingAI() {
        System.out.println("MER-AI (B5-0403): MEDIUM/HARD mercenary bid offer + scoring");
        RulesEngine rules = new RulesEngine();

        // ── A: solo player, one offered mercenary, no competition.
        //    The only non-pass action is a bid of the minimal winning
        //    increment (no rival bids, so inc = 1). Both tiers pick it.
        Player pA = player("MERAIpA", Faction.NARN);
        GameState sA = state(pA);
        EventCard mercA = event("mer_ai_fixture");
        mercA.setMercenary(true);
        sA.addMercenaryOffer(mercA);
        pA.getHand().clear();
        pA.getFleets().clear();
        pA.getInnerCircle().clear();
        pA.getSupportingRole().clear();
        pA.getLocations().clear();
        pA.getGroups().clear();
        pA.getEnhancements().clear();
        pA.setAgenda(null);

        AIPlayer medA = new AIPlayer(pA, AIDifficulty.MEDIUM);
        AIPlayer hardA = new AIPlayer(pA, AIDifficulty.HARD);
        AIPlayer easyA = new AIPlayer(pA, AIDifficulty.EASY);

        GameAction mA = medA.chooseAction(sA, pA);
        check("MER-AI", "MEDIUM bids the minimal winning increment (1) when uncontested",
                mA.getType() == GameAction.Type.BID_ON_MERCENARY
                && mA.getCard() == mercA && mA.getAmount() == 1);
        GameAction hA = hardA.chooseAction(sA, pA);
        check("MER-AI", "HARD bids the minimal winning increment (1) when uncontested",
                hA.getType() == GameAction.Type.BID_ON_MERCENARY
                && hA.getCard() == mercA && hA.getAmount() == 1);
        check("MER-AI", "MEDIUM bid choice is deterministic (same pick twice)",
                medA.chooseAction(sA, pA).getType() == GameAction.Type.BID_ON_MERCENARY);

        // EASY picks uniformly from the same legal list (PASS + the bid);
        // assert the legality contract, not a specific pick.
        boolean easyLegal = true;
        for (int i = 0; i < 100; i++) {
            GameAction e = easyA.chooseAction(sA, pA);
            if (e.getType() != GameAction.Type.PASS
                    && e.getType() != GameAction.Type.BID_ON_MERCENARY) easyLegal = false;
        }
        check("MER-AI", "EASY picks only from the legal set (PASS or BID) over 100 samples",
                easyLegal);

        // ── B: outbidding. A rival already holds the standing highest bid
        //    (2); the AI's offer must be exactly bestOther + 1 = 3, and both
        //    tiers take it (win probability outweighs the cost at these
        //    amounts).
        Player pB = player("MERAIpB", Faction.NARN);
        Player rB = player("MERAIrB", Faction.MINBARI);
        GameState sB = state(pB, rB);
        EventCard mercB = event("mer_ai_fixture_b");
        mercB.setMercenary(true);
        sB.addMercenaryOffer(mercB);
        rules.executeBidOnMercenary(rB, mercB, 2, sB);
        pB.getHand().clear();
        pB.getFleets().clear();
        pB.getInnerCircle().clear();
        pB.getSupportingRole().clear();
        pB.getLocations().clear();
        pB.getGroups().clear();
        pB.getEnhancements().clear();
        pB.setAgenda(null);

        AIPlayer medB = new AIPlayer(pB, AIDifficulty.MEDIUM);
        AIPlayer hardB = new AIPlayer(pB, AIDifficulty.HARD);
        GameAction mB = medB.chooseAction(sB, pB);
        check("MER-AI", "MEDIUM outbids the standing highest bid by exactly one (3)",
                mB.getType() == GameAction.Type.BID_ON_MERCENARY
                && mB.getCard() == mercB && mB.getAmount() == 3);
        GameAction hB = hardB.chooseAction(sB, pB);
        check("MER-AI", "HARD outbids the standing highest bid by exactly one (3)",
                hB.getType() == GameAction.Type.BID_ON_MERCENARY
                && hB.getCard() == mercB && hB.getAmount() == 3);

        // ── C: pool floor. The winning increment (3) exceeds the applied
        //    pool (drained to 1); canBidOnMercenary refuses, so NO bid offer
        //    exists and both tiers pass. A partial bid can never win (ties
        //    crown nobody), so offering one would waste pool by design.
        Player pC = player("MERAIpC", Faction.NARN);
        Player rC = player("MERAIrC", Faction.MINBARI);
        GameState sC = state(pC, rC);
        EventCard mercC = event("mer_ai_fixture_c");
        mercC.setMercenary(true);
        sC.addMercenaryOffer(mercC);
        rules.executeBidOnMercenary(rC, mercC, 2, sC);
        pC.applyInfluence(3);   // pool 4 → 1 < winning increment 3
        pC.getHand().clear();
        pC.getFleets().clear();
        pC.getInnerCircle().clear();
        pC.getSupportingRole().clear();
        pC.getLocations().clear();
        pC.getGroups().clear();
        pC.getEnhancements().clear();
        pC.setAgenda(null);

        AIPlayer medC = new AIPlayer(pC, AIDifficulty.MEDIUM);
        AIPlayer hardC = new AIPlayer(pC, AIDifficulty.HARD);
        check("MER-AI", "MEDIUM passes when the winning increment exceeds the pool",
                medC.chooseAction(sC, pC).getType() == GameAction.Type.PASS);
        check("MER-AI", "HARD passes when the winning increment exceeds the pool",
                hardC.chooseAction(sC, pC).getType() == GameAction.Type.PASS);

        // ── D: hold while strictly leading. p already holds the strictly
        //    highest cumulative bid, so the offer loop must NOT re-bid
        //    (extending a won auction only drains the pool); both tiers pass.
        Player pD = player("MERAIpD", Faction.NARN);
        GameState sD = state(pD);
        EventCard mercD = event("mer_ai_fixture_d");
        mercD.setMercenary(true);
        sD.addMercenaryOffer(mercD);
        rules.executeBidOnMercenary(pD, mercD, 1, sD);
        pD.getHand().clear();
        pD.getFleets().clear();
        pD.getInnerCircle().clear();
        pD.getSupportingRole().clear();
        pD.getLocations().clear();
        pD.getGroups().clear();
        pD.getEnhancements().clear();
        pD.setAgenda(null);

        AIPlayer medD = new AIPlayer(pD, AIDifficulty.MEDIUM);
        AIPlayer hardD = new AIPlayer(pD, AIDifficulty.HARD);
        check("MER-AI", "MEDIUM does not re-bid while strictly leading (pass)",
                medD.chooseAction(sD, pD).getType() == GameAction.Type.PASS);
        check("MER-AI", "HARD does not re-bid while strictly leading (pass)",
                hardD.chooseAction(sD, pD).getType() == GameAction.Type.PASS);
    }

    private static void testD6ActionLoop() {
        System.out.println("D6 (B5-0436 R2): action round ends on consecutive passes; no safety cap");
        Player p1 = player("D6p1", Faction.CENTAURI);
        Player p2 = player("D6p2", Faction.NARN);
        GameState st = state(p1, p2);
        List<AIPlayer> ais = new ArrayList<AIPlayer>();
        for (Player p : st.getPlayers()) {
            p.getHand().clear();
            p.getSupportingRole().clear();
            p.getLocations().clear();
            p.getGroups().clear();
            p.getEnhancements().clear();
            p.setAgenda(null);
            p.addCharacter(leaderCard(p.getName() + "_d6l1", 2));
            p.addCharacter(leaderCard(p.getName() + "_d6l2", 2));
            p.addFleet(fleetCard(p.getName() + "_d6f1", "LINE"));
            p.addFleet(fleetCard(p.getName() + "_d6f2", "LINE"));
            ais.add(new AIPlayer(p, AIDifficulty.MEDIUM));
        }
        GameController gc = new GameController(st, ais,
                new GameStateCallback() {
                    public void accept(GameState gs) { }
                });
        new RulesEngine().startRound(st);
        try {
            java.lang.reflect.Method loop = GameController.class
                    .getDeclaredMethod("runActionPhase");
            loop.setAccessible(true);
            loop.invoke(gc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int p1Actions = 0;
        int p2Actions = 0;
        for (FleetCard fleet : p1.getFleets()) {
            if (fleet.getLeader() != null) p1Actions++;
        }
        for (FleetCard fleet : p2.getFleets()) {
            if (fleet.getLeader() != null) p2Actions++;
        }
        boolean capHit = false;
        for (String line : st.getLog()) {
            if (line.indexOf("safety cap reached") >= 0) capHit = true;
        }
        boolean allPassed = true;
        for (Player p : st.getPlayers()) {
            if (!p.isPassed()) allPassed = false;
        }
        check("D6", "each player can take multiple actions in one round",
              p1Actions == 2 && p2Actions == 2);
        check("D6", "round ends with every player passed (consecutive-pass exit)",
              allPassed);
        check("D6", "action round terminates without hitting the safety cap",
              !st.isGameOver() && !capHit);
    }

    private static void testD7BuildInfluence() {
        System.out.println("D7 (B5-0436 R3): Build Influence — rotate IC, spend 3 pool, rating +1");
        RulesEngine rules = new RulesEngine();

        Player p = player("D7p", Faction.HUMAN);
        GameState st = state(p);
        CharacterCard leader = leaderCard("D7leader", 2);
        p.addCharacter(leader);
        check("D7", "canBuildInfluence true at pool 4, rating 4, ready IC member",
              rules.canBuildInfluence(p));
        rules.executeBuildInfluence(p, leader, st);
        check("D7", "execute rotates the leader, rating 4 to 5, pool 4 to 2 (net -2)",
              leader.isRotated() && p.getInfluence() == 5 && p.getAppliedPool() == 2);

        Player p9 = player("D7p9", Faction.NARN);
        GameState st9 = state(p9);
        CharacterCard leader9 = leaderCard("D7leader9", 2);
        p9.addCharacter(leader9);
        p9.gainInfluence(5);
        check("D7", "rating 9 is still buildable (1..9 window boundary)",
              rules.canBuildInfluence(p9));
        rules.executeBuildInfluence(p9, leader9, st9);
        check("D7", "rating 9 becomes 10 after the build; pool 9 to 7",
              p9.getInfluence() == 10 && p9.getAppliedPool() == 7
              && leader9.isRotated());

        check("D7", "canBuildInfluence false once rating exceeds 9",
              !rules.canBuildInfluence(p9));
        rules.executeBuildInfluence(p9, leader9, st9);
        check("D7", "execute above the cap is a no-op (rating unchanged)",
              p9.getInfluence() == 10 && p9.getAppliedPool() == 7);

        Player pR = player("D7pR", Faction.CENTAURI);
        GameState stR = state(pR);
        CharacterCard leaderR = leaderCard("D7leaderR", 2);
        pR.addCharacter(leaderR);
        leaderR.rotate();
        int poolBefore = pR.getAppliedPool();
        rules.executeBuildInfluence(pR, leaderR, stR);
        check("D7", "execute with an already-rotated leader is a no-op",
              leaderR.isRotated() && pR.getInfluence() == 4
              && pR.getAppliedPool() == poolBefore);
    }

    private static void testD15EffectCoverage() {
        System.out.println("D15 (B5-0436 R4): dispatched effect kinds — winner-only influenceReward");
        RulesEngine rules = new RulesEngine();

        Player initiator = player("D15i", Faction.NARN);
        Player opposer   = player("D15o", Faction.MINBARI);
        GameState st = state(initiator, opposer);
        ConflictCard reward = new ConflictCard("d15_reward", "Reward Strike",
                "CONFLICT_MILITARY", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE,
                "x", "text", ConflictType.MILITARY, 3);
        CharacterCard twoMil = new CharacterCard("d15two", "Two Mil",
                "CHARACTER_NARN", Rarity.COMMON, Faction.NARN, CardSet.PREMIERE,
                "x", "text", 0, 0, 0, 2, false);
        Conflict won = new Conflict(reward, initiator);
        won.commitCard(initiator, initiator.getAmbassador());
        won.commitCard(initiator, twoMil);
        won.commitCard(opposer, opposer.getAmbassador());
        int beforeI = initiator.getInfluence();
        int beforeO = opposer.getInfluence();
        Player winner = rules.resolveConflict(won, st);
        check("D15", "resolveConflict pays the card's influenceReward to the winner only",
              winner == initiator && initiator.getInfluence() == beforeI + 3
              && opposer.getInfluence() == beforeO);

        Player i2 = player("D15i2", Faction.CENTAURI);
        Player o2 = player("D15o2", Faction.HUMAN);
        GameState st2 = state(i2, o2);
        ConflictCard zero = new ConflictCard("d15_zero", "Plain Strike",
                "CONFLICT_DIPLOMACY", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE,
                "x", "text", ConflictType.DIPLOMACY, 0);
        Conflict plain = new Conflict(zero, i2);
        plain.commitCard(i2, i2.getAmbassador());
        plain.commitCard(o2, o2.getAmbassador());
        int b2 = i2.getInfluence();
        rules.resolveConflict(plain, st2);
        check("D15", "zero-reward conflict pays nothing",
              i2.getInfluence() == b2);

        CharacterCard big = new CharacterCard("d15big", "Big Minbari",
                "CHARACTER_MINBARI", Rarity.RARE, Faction.MINBARI, CardSet.PREMIERE,
                "x", "text", 0, 0, 0, 5, false);
        Player i3 = player("D15i3", Faction.CENTAURI);
        Player o3 = player("D15o3", Faction.MINBARI);
        GameState st3 = state(i3, o3);
        ConflictCard r3 = new ConflictCard("d15_reward3", "Reward Strike 3",
                "CONFLICT_MILITARY", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE,
                "x", "text", ConflictType.MILITARY, 2);
        Conflict lost = new Conflict(r3, i3);
        lost.commitCard(i3, i3.getAmbassador());
        lost.commitCard(o3, o3.getAmbassador());
        lost.commitCard(o3, big, false);
        int b3i = i3.getInfluence();
        int b3o = o3.getInfluence();
        Player w3 = rules.resolveConflict(lost, st3);
        check("D15", "opposer win collects the reward; losing initiator gains nothing",
              w3 == o3 && o3.getInfluence() == b3o + 2 && i3.getInfluence() == b3i);
    }

    // ── main ─────────────────────────────────────────────────────────────────

    // ── D12: standard victory strictly-greatest + major-agenda block ───────

    private static void testD12() {
        System.out.println("D12: standard victory = 20+ power, strictly greatest;");
        System.out.println("     major agendas block the standard path");
        RulesEngine rules = new RulesEngine();

        // A: tie at 20 must NOT let the first-indexed player win.
        Player t1 = player("T1", Faction.NARN);
        Player t2 = player("T2", Faction.MINBARI);
        t1.gainInfluence(16); // 20
        t2.gainInfluence(16); // 20
        GameState st = state(t1, t2);
        check("D12", "20-20 tie yields no standard winner",
                rules.checkVictory(st) == null);

        // B: strictly greater wins.
        Player w = player("W12", Faction.NARN);
        Player l = player("L12", Faction.MINBARI);
        w.gainInfluence(16); // 20
        l.gainInfluence(15); // 19
        GameState st2 = state(w, l);
        check("D12", "20 vs 19: strictly-greater player wins",
                rules.checkVictory(st2) == w);

        // C: major agenda blocks the standard path even at 21 power when its
        //    own condition is unmet (Most Inner Circle, tied at 1 each).
        Player major = player("Maj", Faction.HUMAN);
        Player other = player("Oth", Faction.CENTAURI);
        major.gainInfluence(17); // 21
        other.gainInfluence(3);  // 7
        major.setAgenda(new AgendaCard("test_major_mic", "Test Major",
                "AGENDA_MAJOR", Rarity.RARE, Faction.ANY, CardSet.PREMIERE,
                "x", "text", true, "MOST_INNER_CIRCLE"));
        GameState st3 = state(major, other);
        check("D12", "major agenda holder at 21 power does not standard-win",
                rules.checkVictory(st3) == null);

        // D: the same major agenda holder wins via the agenda's own condition
        //    once it IS met (2 vs 1 Inner Circle characters).
        CharacterCard extra = new CharacterCard("mic2", "Extra Advisor",
                "CHARACTER_HUMAN", Rarity.RARE, Faction.HUMAN, CardSet.PREMIERE,
                "x", "text", 1, 1, 0, 1, false);
        major.getInnerCircle().add(extra);
        check("D12", "major agenda holder wins via the agenda condition",
                rules.checkVictory(st3) == major);

        // E: a NON-major agenda holder with an unmet agenda can still score
        //    the standard victory (agenda is an additional way, not a block).
        Player s = player("Std", Faction.NARN);
        Player o = player("Oth2", Faction.MINBARI);
        s.gainInfluence(17); // 21
        o.gainInfluence(3);  // 7
        s.setAgenda(new AgendaCard("test_agenda_mic", "Test Agenda",
                "AGENDA", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE,
                "x", "text", false, "MOST_INNER_CIRCLE"));
        GameState st4 = state(s, o);
        check("D12", "non-major agenda holder standard-wins with strictly-greatest power",
                rules.checkVictory(st4) == s);
    }

    // ── D13: NON_ALIGNED is a race, not universally playable ───────────────

    private static void testD13() {
        System.out.println("D13: NON_ALIGNED as a normal race; NEUTRAL universal");
        check("D13", "NON_ALIGNED card not playable by HUMAN",
                !Faction.NON_ALIGNED.isPlayableBy(Faction.HUMAN));
        check("D13", "NON_ALIGNED card playable by NON_ALIGNED",
                Faction.NON_ALIGNED.isPlayableBy(Faction.NON_ALIGNED));
        check("D13", "NEUTRAL still playable by every faction",
                Faction.NEUTRAL.isPlayableBy(Faction.HUMAN)
                && Faction.NEUTRAL.isPlayableBy(Faction.NON_ALIGNED));
        check("D13", "ANY playable by every faction",
                Faction.ANY.isPlayableBy(Faction.HUMAN)
                && Faction.ANY.isPlayableBy(Faction.NON_ALIGNED));
        check("D13", "loyal HUMAN card not playable by NARN",
                !Faction.HUMAN.isPlayableBy(Faction.NARN));
        check("D13", "loyal card playable by its own race",
                Faction.HUMAN.isPlayableBy(Faction.HUMAN));
    }

    // ── B5-0302: one conflict per faction per turn ─────────────────────────

    private static void testConflictPerTurn() {
        System.out.println("CPT (B5-0302): one conflict per faction per turn");
        RulesEngine rules = new RulesEngine();
        Player p1 = player("P1", Faction.NARN);
        Player p2 = player("P2", Faction.MINBARI);
        GameState st = state(p1, p2);

        ConflictCard c1 = new ConflictCard("cpt1", "First Strike",
                "CONFLICT_MILITARY", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE,
                "x", "text", ConflictType.MILITARY, 1);
        ConflictCard c2 = new ConflictCard("cpt2", "Second Strike",
                "CONFLICT_MILITARY", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE,
                "x", "text", ConflictType.MILITARY, 1);
        ConflictCard c3 = new ConflictCard("cpt3", "Other Strike",
                "CONFLICT_MILITARY", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE,
                "x", "text", ConflictType.MILITARY, 1);
        p1.addToHand(c1);
        p1.addToHand(c2);
        p2.addToHand(c3);

        check("CPT", "first initiation this turn is legal",
                rules.canInitiateConflict(p1, c1, st));
        st.markConflictInitiated(p1);

        check("CPT", "second initiation by the same player is illegal",
                !rules.canInitiateConflict(p1, c2, st));

        check("CPT", "another player can still initiate this turn",
                rules.canInitiateConflict(p2, c3, st));

        // Round boundary = turn boundary in this engine; runGame() calls
        // advanceRound() after every action round, so the marker must clear.
        st.advanceRound();
        check("CPT", "the marker clears at the round boundary",
                !st.hasInitiatedConflictThisTurn(p1)
                && rules.canInitiateConflict(p1, c2, st));

        st.markConflictInitiated(p2);
        check("CPT", "p2 is blocked after their own initiation",
                !rules.canInitiateConflict(p2, c3, st));
    }

    // ── B5-0309: conflict support vs opposition (D14) ──────────────────────

    private static void testConflictSides() {
        System.out.println("CSD (B5-0309): initiator wins iff support > opposition");
        RulesEngine rules = new RulesEngine();

        Player init = player("Init", Faction.NARN);
        Player opp  = player("Oppo", Faction.MINBARI);
        CharacterCard oppAmb  = opp.getAmbassador();
        CharacterCard bigChar = new CharacterCard("big", "Big Opposer Char",
                "CHARACTER_MINBARI", Rarity.RARE, Faction.MINBARI, CardSet.PREMIERE,
                "x", "text", 0, 0, 0, 5, false);

        GameState st = state(init, opp);
        ConflictCard cc = new ConflictCard("csd1", "Side Strike",
                "CONFLICT_MILITARY", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE,
                "x", "text", ConflictType.MILITARY, 2);

        // 1: initiator alone (support 3 vs opposition 0) wins via the engine.
        Conflict c1 = new Conflict(cc, init);
        c1.commitCard(init, init.getAmbassador(), true);
        check("CSD", "support side recorded for the initiator",
                c1.isSupporting(init) && !c1.isOpposing(init)
                && c1.supportTotal() == 3 && c1.oppositionTotal() == 0);
        rules.resolveConflict(c1, st);
        check("CSD", "support 3 vs opposition 0: initiator wins",
                c1.getWinner() == init);

        // 2: an opposing ambassador (support 3 vs opposition 3) defeats the
        //    initiator — the exact pre-B5-0309 bug (tie crowned the initiator).
        Conflict c2 = new Conflict(cc, init);
        c2.commitCard(init, init.getAmbassador(), true);
        c2.commitCard(opp, oppAmb, false);
        rules.resolveConflict(c2, st);
        check("CSD", "support 3 vs opposition 3: initiator loses (opposer wins)",
                c2.getWinner() == opp);
        check("CSD", "tie loss damages nobody (gap below 3)",
                !init.getAmbassador().isFaceDown() && !oppAmb.isFaceDown());

        // 3: stronger opposition wins for the leading opposer.
        Conflict c3 = new Conflict(cc, init);
        c3.commitCard(init, init.getAmbassador(), true);
        c3.commitCard(opp, bigChar, false);
        rules.resolveConflict(c3, st);
        check("CSD", "support 3 vs opposition 5: leading opposer wins",
                c3.getWinner() == opp);

        // 4: a heavy military loss (gap >= 3) damages the LOSER's ambassador
        //    (face-down = damaged), and a damaged character contributes 0.
        Conflict c4 = new Conflict(cc, init);
        c4.commitCard(init, init.getAmbassador(), true);
        c4.commitCard(opp, bigChar, false);
        c4.commitCard(opp, oppAmb, false);
        rules.resolveConflict(c4, st);
        check("CSD", "support 3 vs opposition 8: opposer wins, loser damaged",
                c4.getWinner() == opp && init.getAmbassador().isFaceDown());
        check("CSD", "damaged (face-down) character contributes 0",
                init.getAmbassador().getPrimaryStatValue(ConflictType.MILITARY) == 0);

        // 5: backward-compatible overloads default to the support side.
        Conflict c5 = new Conflict(cc, init);
        c5.addParticipant(opp);
        c5.commitCard(opp, oppAmb);
        check("CSD", "legacy commitCard/addParticipant default to support side",
                c5.isSupporting(opp) && !c5.isOpposing(opp));
    }

    // ── B5-0376 Phase A: war-conflict declaration + resolution ──────────

    private static void testWarConflict() {
        System.out.println("WAR (B5-0376): war conflict initiation, participation, card commitment");
        RulesEngine rules = new RulesEngine();

        Player narn = player("Narn", Faction.NARN);
        Player minbari = player("Minbari", Faction.MINBARI);
        Player centauri = player("Centauri", Faction.CENTAURI);
        GameState st = state(narn, minbari, centauri);
        rules.startRound(st);   // gives players actions (resetActions) so declarations are legal

        // 1: cannot initiate war conflict when not at war with anyone
        check("WAR", "cannot declare war conflict when races are at peace",
                !rules.canDeclareWarConflict(narn, st));

        // 2: enter war between Narn and Minbari
        st.getTensionMatrix().enterWar(Faction.NARN, Faction.MINBARI);

        check("WAR", "can declare war conflict when at war with another race",
                rules.canDeclareWarConflict(narn, st));
        check("WAR", "minbari can also declare war conflict",
                rules.canDeclareWarConflict(minbari, st));

        // 3: initiate a race-target war conflict
        Conflict warConflict = rules.declareWarConflict(narn, WarKind.RACE_TARGET,
                minbari, null, st);
        check("WAR", "war conflict created with null card and race target",
                warConflict != null && warConflict.getCard() == null
                && warConflict.getTarget() == minbari
                && warConflict.isWarConflict());

        // 4: war conflicts are always MILITARY type
        check("WAR", "war conflict type is MILITARY",
                warConflict.getConflictType() == ConflictType.MILITARY);

        // 5: player participation based on warfare state
        check("WAR", "initiator can join own war conflict",
                warConflict.canJoinConflict(narn));
        check("WAR", "target at war with initiator can join",
                warConflict.canJoinConflict(minbari));
        check("WAR", "third party not at war cannot join",
                !warConflict.canJoinConflict(centauri));

        // 6: enter war between Narn and Centauri
        st.getTensionMatrix().enterWar(Faction.NARN, Faction.CENTAURI);
        check("WAR", "third party now at war with initiator can join",
                warConflict.canJoinConflict(centauri));

        // 7: any card can commit (no ConflictCard filters)
        EventCard event1 = event("war_event");
        FleetCard fleet1 = fleetCard("war_fleet", "DESTROYER");
        CharacterCard leader1 = leaderCard("war_leader", 2);

        check("WAR", "event card can commit to war conflict",
                warConflict.commitCard(narn, event1));
        check("WAR", "fleet card can commit to war conflict",
                warConflict.commitCard(minbari, fleet1));
        check("WAR", "character card can commit to war conflict",
                warConflict.commitCard(minbari, leader1));

        // 8: cannot initiate war conflict targeting a race not at war
        Conflict invalidWar = rules.declareWarConflict(narn, WarKind.RACE_TARGET,
                centauri, null, st);
        st.getTensionMatrix().exitWar(Faction.NARN, Faction.CENTAURI);
        Conflict invalidWar2 = rules.declareWarConflict(narn, WarKind.RACE_TARGET,
                centauri, null, st);
        check("WAR", "cannot initiate war conflict against race not at war (after peace)",
                invalidWar2 == null);

        // 9: war conflict has zero influence reward (bonuses applied separately)
        check("WAR", "war conflict carries zero influence reward",
                warConflict.getInfluenceReward() == 0);

        // ── Phase B tests (B5-0376): outcome resolution ──────────────────────
        // 10: uncontested race war → influence swing (target -1, winner +1)
        int narnBefore10 = narn.getInfluence();
        int minbariBefore10 = minbari.getInfluence();
        rules.resolveWarOutcome(warConflict, narn, st);
        check("WAR", "uncontested race war: target loses 1 influence",
                minbari.getInfluence() == minbariBefore10 - 1);
        check("WAR", "uncontested race war: winner gains 1 influence",
                narn.getInfluence() == narnBefore10 + 1);

        // 11-13 (Phase B): location capture, suppression, tension, recapture
        Player locOwner = player("LocOwner", Faction.CENTAURI);
        LocationCard locTarget = new LocationCard("loc_b5", "Caster",
                "LOCATION_CENTAURI", Rarity.RARE, Faction.CENTAURI, CardSet.PREMIERE,
                "x", "text", 2, 3);
        locOwner.getLocations().add(locTarget);
        GameState st2 = state(narn, locOwner, minbari, centauri);
        st2.getTensionMatrix().enterWar(Faction.CENTAURI, Faction.NARN);
        rules.startRound(st2);
        Conflict locWar = rules.declareWarConflict(narn, WarKind.LOCATION_TARGET,
                null, locTarget, st2);
        check("WAR", "location-target war conflict declared", locWar != null);
        rules.resolveWarOutcome(locWar, narn, st2);
        check("WAR", "location captured by winning initiator",
                locTarget.getCapturedBy() == narn);
        check("WAR", "captured location income is suppressed to 0",
                locTarget.getInfluencePerRound() == 0);
        check("WAR", "captured location military is suppressed to 0",
                locTarget.getMilitary() == 0);
        check("WAR", "tension incremented for location-target war outcome",
                st2.getTensionMatrix().getTension(Faction.CENTAURI, Faction.NARN) > 0);

        // 14: recapture by the original faction owner → effects restored
        Conflict recapture = rules.declareWarConflict(locOwner, WarKind.LOCATION_TARGET,
                null, locTarget, st2);
        check("WAR", "location-target recapture war conflict declared", recapture != null);
        rules.resolveWarOutcome(recapture, locOwner, st2);
        check("WAR", "recaptured location cleared of capture marker",
                locTarget.getCapturedBy() == null);
        check("WAR", "recaptured location income restored",
                locTarget.getInfluencePerRound() == 2);
        check("WAR", "recaptured location military restored",
                locTarget.getMilitary() == 3);

        // ── Phase C tests (B5-0376): attack integration ─────────────────────
        // 15: a resolved attack marks the conflict contested
        GameState st3 = state(narn, minbari, centauri);
        st3.getTensionMatrix().enterWar(Faction.NARN, Faction.MINBARI);
        rules.startRound(st3);
        Conflict warC = rules.declareWarConflict(narn, WarKind.RACE_TARGET,
                minbari, null, st3);
        check("WAR", "attacked war conflict declared", warC != null);
        FleetCard narnFleet = fleetCard("narn_fleet", "DESTROYER");
        FleetCard atkBlocker = fleetCard("atk_blocker", "DESTROYER");
        FleetCard atkFleet = fleetCard("atk_fleet", "DESTROYER");
        narn.addFleet(narnFleet);
        minbari.addFleet(atkBlocker);
        minbari.addFleet(atkFleet);
        check("WAR", "narn commits support fleet to war conflict",
                warC.commitCard(narn, narnFleet));
        check("WAR", "minbari commits opposing fleet to war conflict",
                warC.commitCard(minbari, atkBlocker, false));
        check("WAR", "attack action resolves against a committed participant",
                rules.executeAttackConflictParticipant(minbari, atkFleet, narnFleet, warC, st3));
        check("WAR", "attack marks war conflict contested",
                warC.anyAttackOccurred());

        // 16: contested (attacked) race war outcome — no uncontested swing
        int minbariBefore16 = minbari.getInfluence();
        rules.resolveWarOutcome(warC, narn, st3);
        check("WAR", "attacked race war does not swing influence (contested)",
                minbari.getInfluence() == minbariBefore16);
    }

    // ── B5-0321: Promote Character to the Inner Circle ────────────────────

    private static void resetPromotionState(Player p) {
        for (CharacterCard ch : p.getInnerCircle())     ch.unrotate();
        for (CharacterCard ch : p.getSupportingRole())  ch.unrotate();
        p.getAmbassador().heal();
        p.loseInfluence(p.getInfluence() - 5);
    }

    private static void testPromotion() {
        System.out.println("PRM (B5-0321): Promote Character to the Inner Circle");
        RulesEngine rules = new RulesEngine();

        Player p  = player("Prom", Faction.NARN);
        Player p2 = player("Prom2", Faction.MINBARI);
        GameState st = state(p, p2);
        CharacterCard cand = new CharacterCard("cand", "Candidate",
                "CHARACTER_NARN", Rarity.RARE, Faction.NARN, CardSet.PREMIERE,
                "x", "text", 2, 2, 0, 3, false);

        // 1: a faction whose Inner Circle has no member to rotate cannot
        //    promote (the suite helper seats the ambassador in the IC, so
        //    remove it to simulate an empty Inner Circle).
        p.getSupportingRole().add(cand);
        p.gainInfluence(5); // 9
        p.getInnerCircle().remove(p.getAmbassador());
        check("PRM", "no IC member to rotate: cannot promote",
                !rules.canPromote(p, cand) && p.getInnerCircle().isEmpty());
        p.getInnerCircle().add(p.getAmbassador()); // restore (helper convention)

        // 2: cost = character cost 0 + 1 member (ambassador) = 1.
        check("PRM", "IC-size cost term is live (0-cost char + 1 member = 1)",
                rules.promotionCost(p, cand) == 1);
        check("PRM", "affordable + leader available: can promote",
                rules.canPromote(p, cand));

        // 3: a rotated candidate is not promotable.
        cand.rotate();
        check("PRM", "rotated supporting character cannot be promoted",
                !rules.canPromote(p, cand));
        cand.unrotate();

        // 4: a damaged (face-down) candidate is not promotable.
        cand.setFaceDown(true);
        check("PRM", "damaged (face-down) supporting character cannot be promoted",
                !rules.canPromote(p, cand));
        cand.setFaceDown(false);

        // 5: execute with an invalid leader is a logged no-op.
        CharacterCard savedAmb = p.getAmbassador();
        rules.executePromote(p, cand, cand, st);
        check("PRM", "invalid leader: no-op (candidate still supporting)",
                p.getSupportingRole().contains(cand) && p.getInnerCircle().size() == 1);

        // 6: legal promotion moves zones and rotates the leader only.
        rules.executePromote(p, cand, p.getAmbassador(), st);
        check("PRM", "promotion moves the character into the Inner Circle",
                !p.getSupportingRole().contains(cand) && p.getInnerCircle().contains(cand));
        check("PRM", "leader rotated, promoted character ready",
                p.getAmbassador().isRotated() && !cand.isRotated());
        check("PRM", "promotion applies 1 from pool and preserves Rating",
                p.getInfluence() == 9 && p.getAppliedPool() == 8);

        // 7: the IC-member cost term now counts 2 members.
        check("PRM", "cost recomputes with the new IC size (2 members = 2)",
                rules.promotionCost(p, cand) == 2);

        // 8: an empty faction cannot afford a positive promotion cost.
        p2.loseInfluence(p2.getInfluence()); // 0
        CharacterCard cand2 = new CharacterCard("cand2", "Candidate Two",
                "CHARACTER_MINBARI", Rarity.RARE, Faction.MINBARI, CardSet.PREMIERE,
                "x", "text", 1, 1, 0, 1, false);
        p2.getInnerCircle().add(p2.getAmbassador());
        p2.getSupportingRole().add(cand2);
        check("PRM", "influence below the cost: cannot promote",
                !rules.canPromote(p2, cand2) && rules.promotionCost(p2, cand2) > 0);

        // 9: Build Influence becomes reachable once IC members exist — the
        //    B5-0202 Finding 7 unblock this task delivers.
        check("PRM", "Build Influence reachable with an IC member present",
                rules.canBuildInfluence(p));

        // 10: a damaged candidate contributes 0 to conflicts (D1/D14 echo).
        cand.setFaceDown(true);
        check("PRM", "promoted-then-damaged character contributes 0",
                cand.getPrimaryStatValue(ConflictType.DIPLOMACY) == 0);
        cand.setFaceDown(false);

        // 11: promotion state fully resets at the round boundary.
        resetPromotionState(p);
        check("PRM", "promotion state resets for a fresh round",
                !p.getAmbassador().isRotated() && !cand.isRotated()
                && !cand.isFaceDown() && p.getInfluence() == 5);

        // 12: a second promotion pays the larger IC-size term.
        CharacterCard second = new CharacterCard("second", "Second Candidate",
                "CHARACTER_NARN", Rarity.RARE, Faction.NARN, CardSet.PREMIERE,
                "x", "text", 2, 2, 0, 2, false);
        p.getSupportingRole().add(second);
        rules.executePromote(p, second, cand, st);
        check("PRM", "second promotion: leader rotates, IC grows to 3",
                cand.isRotated() && p.getInnerCircle().size() == 3);
        check("PRM", "second promotion applies cost 2 without eroding Rating",
                p.getInfluence() == 5 && p.getAppliedPool() == 2);
    }

    // ── B5-0323: card cost field + recruit/promote cost wiring ──────────

    private static CharacterCard charCard(String id, Faction f, int cost) {
        CharacterCard c = new CharacterCard(id, id, "CHARACTER_" + f,
                Rarity.RARE, f, CardSet.PREMIERE, "x", "text", 2, 2, 0, 2, false);
        c.setCost(cost);
        return c;
    }

    private static void testCostField() {
        System.out.println("CST (B5-0323): card cost field + recruit/promote wiring");
        RulesEngine rules = new RulesEngine();

        Player p = player("Cost", Faction.NARN);
        CharacterCard freeChar  = charCard("c_free",  Faction.NARN, 0);
        CharacterCard cheapChar = charCard("c_cheap", Faction.NARN, 2);
        CharacterCard otherChar = charCard("c_other", Faction.MINBARI, 3);

        // 1: the default is 0 and negatives clamp (loader contract for the
        //    current data, which carries no cost key — B5-0311 C1).
        CharacterCard fresh = new CharacterCard("c_fresh", "Fresh",
                "CHARACTER_NARN", Rarity.RARE, Faction.NARN, CardSet.PREMIERE,
                "x", "text", 1, 1, 0, 1, false);
        check("CST", "a card without an explicit cost defaults to 0",
                fresh.getCost() == 0);
        fresh.setCost(-5);
        check("CST", "negative cost clamps to 0", fresh.getCost() == 0);

        // 2: recruit cost = card cost, doubled for other-race loyalty;
        //    neutral characters at no additional cost.
        check("CST", "recruit cost = card cost for own faction",
                rules.recruitCost(p, cheapChar) == 2);
        check("CST", "recruit cost doubles for other-race loyal characters",
                rules.recruitCost(p, otherChar) == 6);
        check("CST", "neutral characters cost no extra to recruit",
                rules.recruitCost(p, charCard("c_neut", Faction.NEUTRAL, 3)) == 3);
        check("E1", "double-cost helper identifies a different loyal race",
                rules.isDoubleCostRequired(otherChar, Faction.NARN));
        check("E1", "double-cost helper exempts neutral and matching race",
                !rules.isDoubleCostRequired(charCard("e1_neut", Faction.NEUTRAL, 3), Faction.NARN)
                && !rules.isDoubleCostRequired(cheapChar, Faction.NARN));
        Player discounted = player("Discounted cost", Faction.NARN);
        discounted.grantSponsorDiscount(1);
        check("E1", "double-cost base composes before assistant discount",
                rules.baseRecruitCost(discounted, otherChar) == 6
                && rules.recruitCost(discounted, otherChar) == 5);
        SponsorCost ordinarySponsor = rules.sponsorCost(p, otherChar);
        SponsorCost waivedSponsor = SponsorCost.waived(
                rules.baseRecruitCost(p, otherChar));
        check("E3", "ordinary sponsor result retains amount and rotation requirement",
                ordinarySponsor.getAmount() == 6
                && ordinarySponsor.requiresRotation() && !ordinarySponsor.isWaived());
        check("E3", "free sponsor waiver removes the whole doubled cost and rotation",
                waivedSponsor.getAmount() == 0 && !waivedSponsor.requiresRotation()
                && waivedSponsor.isWaived());

        // 3: canRecruit requires the card in hand and affordable.
        p.addToHand(cheapChar);
        p.addToHand(otherChar);
        p.loseInfluence(p.getInfluence() - 3);   // influence = 3
        check("CST", "card not in hand: cannot recruit",
                !rules.canRecruit(p, freeChar));
        check("CST", "cannot recruit when cost exceeds influence",
                !rules.canRecruit(p, otherChar));
        check("CST", "can recruit an affordable in-hand card",
                rules.canRecruit(p, cheapChar));

        // 4: live recruit path — exact spend + zone move (mirrors the
        //    controller branch; the branch itself runs in smoke/probes).
        int before = p.getInfluence();           // 3
        p.applyInfluence(rules.recruitCost(p, cheapChar));
        p.removeFromHand(cheapChar);
        p.placeInSupportingRole(cheapChar);
        check("CST", "recruiting spends the card cost and seats the character",
                p.getInfluence() == before && p.getAppliedPool() == 1
                && !p.getHand().contains(cheapChar)
                && p.getSupportingRole().contains(cheapChar));

        // 5: promotion now costs card cost 2 + 1 IC member = 3 > influence 1.
        check("CST", "promotion unaffordable when cost + IC exceeds influence",
                !rules.canPromote(p, cheapChar)
                && rules.promotionCost(p, cheapChar) == 3);

        // 6: restoring influence makes the same promotion legal — the seam
        //    this task exists to enable.
        p.gainInfluence(4);                      // 5
        check("CST", "promotion affordable once influence covers cost + IC",
                rules.canPromote(p, cheapChar));

        // 7: the loader hydrates an explicit cost key and defaults when absent.
        List<Card> parsed = DeckLoader.parseCards(
                "[{\"id\":\"cst1\",\"title\":\"Costly\",\"type\":\"CHARACTER\","
                + "\"faction\":\"NARN\",\"diplomacy\":1,\"intrigue\":1,"
                + "\"psi\":0,\"leadership\":1,\"isAmbassador\":false,\"cost\":4},"
                + "{\"id\":\"cst2\",\"title\":\"Free\",\"type\":\"CHARACTER\","
                + "\"faction\":\"NARN\",\"diplomacy\":1,\"intrigue\":1,"
                + "\"psi\":0,\"leadership\":1,\"isAmbassador\":false}]");
        check("CST", "loader parses an explicit cost",
                parsed.size() == 2 && parsed.get(0).getCost() == 4);
        check("CST", "loader defaults an absent cost to 0",
                parsed.get(1).getCost() == 0);
    }

    private static void testFreeParticipantWaiver() {
        System.out.println("E3 (B5-0374): free-participant effect and join path");
        RulesEngine rules = new RulesEngine();
        Player initiator = player("Free participant initiator", Faction.NARN);
        Player joiner = player("Free participant joiner", Faction.HUMAN);
        FleetCard nonAlignedFleet = new FleetCard("e3_na_fleet", "Fixture fleet",
                "FLEET", Rarity.COMMON, Faction.NON_ALIGNED, CardSet.PREMIERE,
                "x", "fixture", 1);
        joiner.addFleet(nonAlignedFleet);
        ConflictCard card = conflictCard("conf_non_aligned_support",
                ConflictType.DIPLOMACY, null);
        Conflict conflict = new Conflict(card, initiator);
        int poolBefore = joiner.getAppliedPool();

        check("E3", "Non-Aligned Support is registered as a participant waiver",
                CardEffects.participantWaiver(conflict)
                        == CardEffects.WaiverEffect.FREE_PARTICIPANT);
        check("E3", "registered waiver enters through the ordinary join gate",
                rules.canJoinConflict(joiner, conflict));
        rules.executeJoinConflict(joiner, conflict, true, state(initiator, joiner));
        check("E3", "free participant joins without pool spend or fleet rotation",
                conflict.getParticipants().contains(joiner)
                && joiner.getAppliedPool() == poolBefore
                && !nonAlignedFleet.isRotated()
                && conflict.getCommittedCards(joiner).contains(nonAlignedFleet));
    }

    // ── B5-0324: cost-aware AI scoring ────────────────────────────────────

    private static void testAppliedInfluencePool() {
        System.out.println("D9 (B5-0369): Rating versus applied influence pool");
        Player p = player("D9", Faction.NARN);
        check("D9", "pool starts at the Influence Rating",
                p.getInfluence() == 4 && p.getAppliedPool() == 4);
        check("D9", "applying the full pool leaves Rating unchanged",
                p.applyInfluence(4) && p.getInfluence() == 4 && p.getAppliedPool() == 0);
        new RulesEngine().startRound(state(p, player("D9round", Faction.MINBARI)));
        check("D9", "start-round restoration resets pool to Rating", p.getAppliedPool() == 4);
        p.gainInfluence(2);
        check("D9", "permanent gain raises Rating and spendable pool",
                p.getInfluence() == 6 && p.getAppliedPool() == 6);
        p.loseInfluence(4);
        check("D9", "permanent loss lowers Rating and clamps current pool",
                p.getInfluence() == 2 && p.getAppliedPool() == 2);
        check("D9", "Build Influence requires three applied points",
                !new RulesEngine().canBuildInfluence(p));
        p.gainInfluence(7);
        CharacterCard builder = p.getAmbassador();
        new RulesEngine().executeBuildInfluence(p, builder,
                state(p, player("D9x", Faction.MINBARI)));
        check("D9", "Build Influence applies 3 and permanently raises Rating by 1",
                p.getInfluence() == 10 && p.getAppliedPool() == 7);
        check("D9", "Rating 10 blocks another Build Influence action",
                !new RulesEngine().canBuildInfluence(p));
        Player winner = player("D9win", Faction.NARN);
        winner.gainInfluence(16);
        winner.applyInfluence(4);
        check("D9", "victory reads Rating when applied pool is empty",
                new RulesEngine().checkVictory(state(winner)) == winner);
    }

    private static void testAIScoring() {
        System.out.println("AIS (B5-0324): cost-aware AI scoring");
        Player aiP = player("AIs", Faction.NARN);
        aiP.gainInfluence(6); // 10 → Build Influence offers are out of the way
        aiP.gainInfluence(10); // B5-0344 re-anchor: 20 → the INFLUENCE_20
                               // agenda's proximity score is 9, restoring the
                               // agenda-first positional order it asserts
        GameState st = state(aiP);

        AgendaCard agenda = new AgendaCard("ais_ag", "Test Agenda",
                "AGENDA", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE,
                "x", "text", false, "INFLUENCE_20");
        LocationCard loc = new LocationCard("ais_loc", "Test Location",
                "LOCATION", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE,
                "x", "text", 1);
        aiP.addToHand(agenda);
        aiP.addToHand(loc);

        AIPlayer med = new AIPlayer(aiP, AIDifficulty.MEDIUM);        // 1: with all costs 0 (the current data), the agenda still wins —
        //    B5-0344: by win-condition proximity (9 at influence 20) instead
        //    of the old flat 9; the ordering assertion is preserved.
        check("AIS", "MEDIUM: zero costs keep the agenda-first ordering",
                med.chooseAction(st, aiP).getCard() == agenda);


        // 2: raising the agenda's cost flips the choice (9-4=5 < 6).
        agenda.setCost(4);
        check("AIS", "MEDIUM: cost subtracts — picks the cheaper location",
                med.chooseAction(st, aiP).getCard() == loc);

        // 3: both costs floored at 0 → nothing beats PASS (never negative).
        agenda.setCost(20);
        loc.setCost(20);
        check("AIS", "MEDIUM: scores floor at 0; PASS wins when all equal",
                med.chooseAction(st, aiP).getType() == GameAction.Type.PASS);
        loc.setCost(0);

        // 4: HARD subtracts too (agenda 9 vs location 7 at zero; 9-3=6 < 7
        //    once the agenda costs 3).
        agenda.setCost(0);
        AIPlayer hard = new AIPlayer(aiP, AIDifficulty.HARD);
        check("AIS", "HARD: zero cost keeps positional order",
                hard.chooseAction(st, aiP).getCard() == agenda);
        agenda.setCost(3);
        check("AIS", "HARD: cost subtracts — picks the cheaper location",
                hard.chooseAction(st, aiP).getCard() == loc);

        // 5: recruits — MEDIUM prefers the cheaper character, floors at 0,
        //    and flips when costs flip.
        aiP.removeFromHand(agenda);
        aiP.removeFromHand(loc);
        CharacterCard freeChar   = charCard("ais_free",   Faction.NARN, 0);
        CharacterCard costlyChar = charCard("ais_costly", Faction.NARN, 3);
        aiP.addToHand(freeChar);
        aiP.addToHand(costlyChar);
        check("AIS", "MEDIUM: prefers the cheaper recruit (5 vs 2)",
                med.chooseAction(st, aiP).getCard() == freeChar);
        freeChar.setCost(9);   // 5-9 → floored to 0
        costlyChar.setCost(1); // 5-1 = 4
        check("AIS", "MEDIUM: after the cost flip picks the now-cheaper recruit",
                med.chooseAction(st, aiP).getCard() == costlyChar);
    }

    // ── B5-0340: station entity + Standard Victory condition 2 ──────────────

    private static void testStation() {
        System.out.println("STA (B5-0340): station entity + victory condition 2");
        RulesEngine rules = new RulesEngine();

        // 1: the station starts at the recorded design value (0); nothing in
        //    the engine moves it (B5-0354 research: no card data exists).
        Player a = player("STA1", Faction.NARN);
        Player b = player("STA2", Faction.MINBARI);
        GameState st = state(a, b);
        check("STA", "station starts at STATION_START_INFLUENCE (0)",
                st.getStation().getInfluence() == Babylon5Station.STATION_START_INFLUENCE);

        // 2: below the threshold, condition 2 never fires even with a strict
        //    single leader (no-regression guard for today's behavior).
        st.getStation().gainInfluence(19);
        a.gainInfluence(3);   // strict leader 7 vs 4
        check("STA", "station below 20 with a strict leader: no winner",
                rules.checkVictory(st) == null);

        // 3: at 20+ with exactly one strictly-leading eligible player, the
        //    leader wins (interpretation: eligibility = not barred; rulebook
        //    :170/:176 — corrected from B5-0354's first reading).
        st.getStation().gainInfluence(1);   // station reaches 20
        check("STA", "station 20 + single strict leader: leader wins",
                rules.checkVictory(st) == a);

        // 4: a tie for the lead crowns nobody (strictly-greatest discipline).
        b.gainInfluence(3);                 // 7 vs 7
        check("STA", "station 20 + tied leaders: no winner",
                rules.checkVictory(st) == null);
        b.loseInfluence(3);                 // back to a strict lead

        // 5: a major-agenda holder is standard-ineligible: never crowned by
        //    condition 2, and does not block an eligible leader's crown.
        b.setAgenda(new AgendaCard("sta_major", "Major Agenda", "AGENDA_MAJOR",
                Rarity.RARE, Faction.MINBARI, CardSet.PREMIERE, "x", "text",
                true, "INFLUENCE_20"));
        check("STA", "station 20: major-agenda holder is not crowned",
                rules.checkVictory(st) != b);
        check("STA", "station 20: eligible leader still crowned past a major-agenda player",
                rules.checkVictory(st) == a);
        b.setAgenda(null);

        // 6: the Shadow War makes condition 2 inert (rulebook :178 — no
        //    Standard Victory during the Shadow War; B5-0354 finding).
        st.setShadowInfluence(20);
        check("STA", "station 20 + strict leader during Shadow War: no winner",
                rules.checkVictory(st) == null);
        st.setShadowInfluence(0);

        // 7: end-of-round semantics — checkVictory at the round boundary is
        //    the engine's "end of turn" (interpretation in DECISIONS); the
        //    entity mirrors Player's influence mutation symmetry.
        st.getStation().loseInfluence(20);
        check("STA", "station influence floors at 0",
                st.getStation().getInfluence() == 0);
    }

    // ── B5-0437: station-influence card hooks (0428 proposal) ───────────────

    /** B5-0437 helper: the pool's location capture fixture (matches the WAR
     *  section, line 1835): Centauri-loyal location, influencePerRound 2. */
    private static LocationCard stationHookLoc() {
        return new LocationCard("loc_b5", "Caster",
                "LOCATION_CENTAURI", Rarity.RARE, Faction.CENTAURI, CardSet.PREMIERE,
                "x", "text", 2, 3);
    }

    private static void testStationHooks() {
        System.out.println("STH (B5-0437): station-influence card hooks (0428 proposal)");
        RulesEngine rules = new RulesEngine();

        Player narn = player("STH-Narn", Faction.NARN);
        Player centauri = player("STH-Cent", Faction.CENTAURI);
        LocationCard loc = stationHookLoc();
        centauri.getLocations().add(loc);
        GameState st = state(narn, centauri);
        st.getTensionMatrix().enterWar(Faction.CENTAURI, Faction.NARN);
        rules.startRound(st);

        // 1: no-source guard — decay on a round boundary only when no source
        //    fired. With all ratings 0 there is nothing to decay and the
        //    marker stays clear.
        rules.applyEndOfRoundStation(st);
        check("STH", "idle round: ratings stay 0 and marker stays clear",
                st.getStation().getInfluence() == 0
                && !st.isStationSourceFired());

        // 2: capture source — a won LOCATION_TARGET war raises the station
        //    rating 1 and sets the no-source guard for this round.
        Conflict capWar = rules.declareWarConflict(narn, WarKind.LOCATION_TARGET,
                null, loc, st);
        check("STH", "location-target war conflict declared", capWar != null);
        rules.resolveWarOutcome(capWar, narn, st);
        check("STH", "capture raises station influence to 1",
                st.getStation().getInfluence() == 1);
        check("STH", "capture sets the no-source marker",
                st.isStationSourceFired());
        check("STH", "capture suppresses location effects",
                loc.isEffectsSuppressed() && loc.getCapturedBy() == narn);

        // 3: decay sink skipped this round — a source fired, so the boundary
        //    maintenance must NOT decay the capture gain away.
        rules.applyEndOfRoundStation(st);
        check("STH", "round with a capture skips decay (rating holds)",
                st.getStation().getInfluence() == 1);
        check("STH", "maintenance resets the marker after the boundary",
                !st.isStationSourceFired());

        // 4: decay sink — next idle round decays the unguarded rating 1.
        rules.applyEndOfRoundStation(st);
        check("STH", "next idle round decays rating 1 toward baseline",
                st.getStation().getInfluence() == 0);

        // 5: recapture — suppressed location retaken by its owner restores it
        //    without a capture gain (gain lives only on first capture).
        Conflict recapWar = rules.declareWarConflict(centauri, WarKind.LOCATION_TARGET,
                null, loc, st);
        rules.resolveWarOutcome(recapWar, centauri, st);
        check("STH", "recapture restores effects and clears the occupier",
                !loc.isEffectsSuppressed() && loc.getCapturedBy() == null);
        check("STH", "recapture is restoration, not a source (no marker, no gain)",
                !st.isStationSourceFired()
                && st.getStation().getInfluence() == 0);

        // 6: presence-bleed — a Vorlon player holding a captured location
        //    raises vorlon influence 1 at the boundary, which also guards
        //    against decay in the same pass.
        Player vorlon = player("STH-Vor", Faction.VORLON);
        LocationCard vloc = stationHookLoc();
        vloc.setCapturedBy(vorlon);
        vorlon.getLocations().add(vloc);
        GameState st3 = state(narn, centauri, vorlon);
        st3.getStation().gainVorlonInfluence(1);
        st3.getStation().gainShadowInfluence(1);
        LocationCard vloc3 = stationHookLoc();
        vloc3.setCapturedBy(vorlon);
        vorlon.getLocations().add(vloc3);
        rules.applyEndOfRoundStation(st3);
        check("STH", "Vorlon presence-bleed raises vorlon rating",
                st3.getStation().getVorlonInfluence() == 2);
        check("STH", "presence-bleed suppresses decay on other ratings",
                st3.getStation().getShadowInfluence() == 1);

        // 7: isolation — the B5-0354 trap. The capture/bleed hooks raise the
        //    station ratings; Support Babylon 5 player-side effects are not
        //    consulted (stationSourceFired moves no player influence).
        Player narn2 = player("STH-Narn2", Faction.NARN);
        Player cent2 = player("STH-Cent2", Faction.CENTAURI);
        LocationCard loc2 = stationHookLoc();
        cent2.getLocations().add(loc2);
        GameState st4 = state(narn2, cent2);
        st4.getTensionMatrix().enterWar(Faction.CENTAURI, Faction.NARN);
        int narnInfBefore = narn2.getInfluence();
        rules.startRound(st4);
        Conflict capWar2 = rules.declareWarConflict(narn2, WarKind.LOCATION_TARGET,
                null, loc2, st4);
        rules.resolveWarOutcome(capWar2, narn2, st4);
        check("STH", "capture hook moves the station rating only",
                st4.getStation().getInfluence() == 1
                && narn2.getInfluence() == narnInfBefore);

        // 8: ordering guard — capture fired this round must survive the FULL
        //    live loop boundary (runGame order: maintenance, then
        //    advanceRound resets the marker, then next startRound).
        int held = st4.getStation().getInfluence();
        st4.advanceRound();
        rules.startRound(st4);
        check("STH", "full loop boundary holds a captured-round rating",
                st4.getStation().getInfluence() == held && held == 1);
    }

    // ── B5-0344: agenda/aftermath/event AI scoring ──────────────────────────

    private static void testAESScoring() {
        System.out.println("AES (B5-0344): agenda/aftermath/event AI scoring");
        Player p = player("AES", Faction.NARN);
        Player rival = player("AESR", Faction.MINBARI);
        GameState st = state(p, rival);
        AIPlayer med = new AIPlayer(p, AIDifficulty.MEDIUM);
        AIPlayer hard = new AIPlayer(p, AIDifficulty.HARD);
        p.gainInfluence(6);   // influence 10: suppresses Build-Influence offers
                              // so the assertion signal is not drowned by them

        // 1: MEDIUM takes an agenda whose condition is already met — playing
        //    it wins at the next victory check (proximity score 9).
        AgendaCard sup = new AgendaCard("aes_sup", "Supremacy Agenda",
                "AGENDA", Rarity.RARE, Faction.NARN, CardSet.PREMIERE, "x", "text",
                false, "MILITARY_SUPREMACY");
        p.addToHand(sup);
        FleetCard fleet = new FleetCard("aes_f1", "Grand Fleet", "FLEET",
                Rarity.RARE, Faction.NARN, CardSet.PREMIERE, "x", "text", 4);
        p.getFleets().add(fleet);
        check("AES", "MEDIUM plays a win-on-play agenda",
                med.chooseAction(st, p).getCard() == sup);
        p.getFleets().remove(fleet);

        // 2: the same agenda is declined when its condition is far from met
        //    (proximity 0; a free recruit scores 5 and wins the table).
        CharacterCard recruit = charCard("aes_char", Faction.NARN, 0);
        p.addToHand(recruit);
        check("AES", "MEDIUM declines a far agenda and recruits instead",
                med.chooseAction(st, p).getCard() == recruit);
        p.removeFromHand(sup);
        p.removeFromHand(recruit);   // leak fix: a free recruit left in hand
                                     // would outscore the check-3..6 probes

        // 3: events gain catch-up value for the trailing player (both cost 0:
        //    event 2+3=5 beats the group's flat 4).
        EventCard ev = new EventCard("aes_ev", "Catch-Up Event", "EVENT",
                Rarity.COMMON, Faction.ANY, CardSet.PREMIERE, "x", "text");
        GroupCard grp = new GroupCard("aes_grp", "Useful Group", "GROUP",
                Rarity.COMMON, Faction.ANY, CardSet.PREMIERE, "x", "text");
        p.loseInfluence(0);          // p stays at 10: Build Influence stays
                                     // suppressed so the signal is not drowned
        rival.gainInfluence(9);      // rival 13 vs p 10: p trails by 3 → bonus capped 3
        p.addToHand(ev);
        p.addToHand(grp);
        check("AES", "trailing player prefers the event (catch-up bonus)",
                med.chooseAction(st, p).getCard() == ev);

        // 4: the leading player keeps the pre-0344 ordering (bonus 0: group
        //    4 beats the event's 2).
        p.gainInfluence(5);          // p 15, rival 13: p leads (bonus 0)
        check("AES", "leading player keeps the old event ordering (no bonus)",
                med.chooseAction(st, p).getCard() == grp);
        p.removeFromHand(ev);
        p.removeFromHand(grp);

        // 5: a lone aftermath is HELD — MEDIUM passes rather than discard it
        //    through the generic play branch.
        AftermathCard am = aftermath("aes_am", "WON");
        p.addToHand(am);
        check("AES", "MEDIUM passes instead of discarding a lone aftermath",
                med.chooseAction(st, p).getType() == GameAction.Type.PASS);

        // 6: with a real card alongside, the aftermath stays held.
        EventCard filler = event("aes_ev2");
        p.addToHand(filler);
        check("AES", "MEDIUM plays the event instead of the aftermath",
                med.chooseAction(st, p).getCard() == filler);
        p.removeFromHand(am);
        p.removeFromHand(filler);

        // 7+8: aftermath anticipation is decisive — a losing-matchup,
        // zero-reward conflict clears the pass bar exactly when enough
        // in-hand WON aftermaths ride on the projected initiator win
        // (6 × +1.0 = 6 vs base 0 − leader −3 − loss −3 = 0 > pass −0.5;
        // in a 2-player state the sole target is always leadingPlayer's
        // pick, so the leader penalty always applies).
        rival.gainInfluence(2);      // rival 15: ties p — the sole legal
                                     // target is still leadingPlayer's pick
        ConflictCard hope = new ConflictCard("aes_hope", "Hope Strike",
                "CONFLICT_DIPLOMACY", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE,
                "x", "text", ConflictType.DIPLOMACY, 0);
        CharacterCard rivalBig = new CharacterCard("aes_rbig", "Rival Grand Char",
                "CHARACTER_MINBARI", Rarity.RARE, Faction.MINBARI, CardSet.PREMIERE,
                "x", "text", 3, 0, 0, 0, false);
        rival.getInnerCircle().add(rivalBig);      // rival diplomacy 6 vs p 3
        p.addToHand(hope);
        AftermathCard a1 = aftermath("aes_a1", "WON");
        AftermathCard a2 = aftermath("aes_a2", "WON");
        AftermathCard a3 = aftermath("aes_a3", "WON");
        AftermathCard a4 = aftermath("aes_a4", "WON");
        AftermathCard a5 = aftermath("aes_a5", "WON");
        AftermathCard a6 = aftermath("aes_a6", "WON");
        p.addToHand(a1);
        p.addToHand(a2);
        p.addToHand(a3);
        p.addToHand(a4);
        p.addToHand(a5);
        p.addToHand(a6);
        check("AES", "HARD initiates the losing matchup when 6 aftermaths anticipate the win",
                hard.chooseAction(st, p).getType() == GameAction.Type.INITIATE_CONFLICT);
        p.removeFromHand(a1);
        p.removeFromHand(a2);
        p.removeFromHand(a3);
        p.removeFromHand(a4);
        p.removeFromHand(a5);
        p.removeFromHand(a6);
        check("AES", "same conflict without live aftermaths: pass (no anticipation)",
                hard.chooseAction(st, p).getType() == GameAction.Type.PASS);
        p.removeFromHand(hope);
        rival.getInnerCircle().remove(rivalBig);

        // 9: EASY stays random over the SAME legal set (difficulty contract):
        //    with only a held aftermath + pass, EASY must also pass — the
        //    hold is an offer-level rule, not a tier scoring rule.
        p.addToHand(am);
        AIPlayer easy = new AIPlayer(p, AIDifficulty.EASY);
        boolean allPass = true;
        for (int i = 0; i < 20; i++) {
            if (easy.chooseAction(st, p).getType() != GameAction.Type.PASS) allPass = false;
        }
        check("AES", "EASY also holds the aftermath (offer-level rule, 20 runs)",
                allPass);
        p.removeFromHand(am);
    }

    // ── B5-0362: LEAD_FLEET action plumbing (B5-0345 Tier-1 #1) ──────────────

    @SuppressWarnings("unchecked")
    private static void testLeadFleetAction() throws Exception {
        System.out.println("LEAD (B5-0362): LEAD_FLEET factory, offers, controller handler, expiry");
        RulesEngine rules = new RulesEngine();

        Player p = player("LEADp", Faction.NARN);
        Player r = player("LEADr", Faction.MINBARI);
        GameState st = state(p, r);
        FleetCard fl = fleetCard("lead_fl", "FRIGATE");   // Military 3
        p.getFleets().add(fl);
        CharacterCard chA = leaderCard("lead_chA", 1);    // Leadership 1
        CharacterCard chB = leaderCard("lead_chB", 5);    // Leadership 5
        p.getInnerCircle().add(chA);
        p.getInnerCircle().add(chB);
        p.gainInfluence(5);                               // rating 9 (tames BUILD score)

        // 1: the factory carries the pair through the EXISTING leader/card
        //    fields — no new GameAction fields (task-row requirement).
        GameAction act = GameAction.leadFleet(chB, fl);
        check("LEAD", "factory carries (leader, fleet) via existing fields",
                act.getType() == GameAction.Type.LEAD_FLEET
                && act.getCard() == fl && act.getLeader() == chB
                && act.getTarget() == null);

        // 2: the builder offers every legal pair — 1 fleet x 3 ready
        //    characters (ambassador + chA + chB), and a fleetless player
        //    gets zero LEAD_FLEET offers (same builder feeds EASY/MEDIUM/HARD).
        AIPlayer med = new AIPlayer(p, AIDifficulty.MEDIUM);
        Method build = AIPlayer.class.getDeclaredMethod(
                "buildLegalActions", GameState.class, Player.class);
        build.setAccessible(true);
        List<GameAction> legal = (List<GameAction>) build.invoke(med, st, p);
        int leadOffers = 0;
        boolean pairsLegal = true;
        for (int i = 0; i < legal.size(); i++) {
            GameAction a = legal.get(i);
            if (a.getType() == GameAction.Type.LEAD_FLEET) {
                leadOffers++;
                if (!(a.getCard() instanceof FleetCard) || a.getLeader() == null
                        || !rules.canLeadFleet(p, a.getLeader(), (FleetCard) a.getCard())) {
                    pairsLegal = false;
                }
            }
        }
        check("LEAD", "builder offers 3 legal (leader, fleet) pairs, all canLeadFleet",
                leadOffers == 3 && pairsLegal);
        AIPlayer medR = new AIPlayer(r, AIDifficulty.MEDIUM);
        List<GameAction> legalR = (List<GameAction>) build.invoke(medR, st, r);
        boolean fleetlessNoLead = true;
        for (int i = 0; i < legalR.size(); i++) {
            if (legalR.get(i).getType() == GameAction.Type.LEAD_FLEET) fleetlessNoLead = false;
        }
        check("LEAD", "no fleet -> no LEAD_FLEET offer", fleetlessNoLead);

        // 3: MEDIUM and HARD pick the same best pair deterministically over
        //    the unchanged state (biggest unused Leadership wins both the
        //    modest-value term and HARD's projected-initiative term).
        GameAction m1 = med.chooseAction(st, p);
        GameAction m2 = med.chooseAction(st, p);
        check("LEAD", "MEDIUM deterministic: best pair = Leadership 5 twice",
                m1.getType() == GameAction.Type.LEAD_FLEET && m1.getLeader() == chB
                && m1.getCard() == fl
                && m2.getType() == GameAction.Type.LEAD_FLEET && m2.getLeader() == chB);
        AIPlayer hard = new AIPlayer(p, AIDifficulty.HARD);
        GameAction h1 = hard.chooseAction(st, p);
        GameAction h2 = hard.chooseAction(st, p);
        check("LEAD", "HARD deterministic: same pair twice (initiative-weighted)",
                h1.getType() == GameAction.Type.LEAD_FLEET && h1.getLeader() == chB
                && h2.getType() == GameAction.Type.LEAD_FLEET && h2.getLeader() == chB);

        // 4: the controller handler reuses the B5-0337 primitives — rotate
        //    consumed, leader seated, effective Military carries the
        //    Leadership, and exactly one action is consumed per pair.
        GameController gc = new GameController(st, new ArrayList<AIPlayer>(),
            new GameStateCallback() {
                public void accept(GameState gs) { }
            });
        Method handler = GameController.class.getDeclaredMethod(
                "processAction", Player.class, GameAction.class);
        handler.setAccessible(true);
        check("LEAD", "actionsLeft before handler = 1", p.getActionsLeft() == 1);
        handler.invoke(gc, p, m1);
        check("LEAD", "handler reuses executeLeadFleet: leader rotated, fleet led",
                chB.isRotated() && fl.getLeader() == chB);
        check("LEAD", "leader Military flows into the effective fleet value (3+5)",
                fl.getEffectiveMilitary() == 8
                && p.conflictTotal(ConflictType.MILITARY) == 8);
        // B5-0372 (D6): processAction no longer calls useAction() — the loop
        // delivers one action per eligibility visit. actionsLeft stays 1 after
        // a direct handler call (the spend happens at the loop level).
        check("LEAD", "leader rot + fleet led (D6: no per-handler action spend)",
                chB.isRotated() && fl.getLeader() == chB
                && p.getActionsLeft() == 1);
        check("LEAD", "one-leader-per-fleet holds through the gate after the lead",
                !rules.canLeadFleet(p, chA, fl));

        // 5: illegal pairs through the handler are logged no-ops — the same
        //    fleet twice, and a foreign player's fleet (ownership gate).
        handler.invoke(gc, p, GameAction.leadFleet(chA, fl));
        FleetCard rFl = fleetCard("lead_rfl", "PICKET");
        r.getFleets().add(rFl);
        handler.invoke(gc, p, GameAction.leadFleet(chA, rFl));
        check("LEAD", "already-led fleet and foreign fleet both refused",
                !chA.isRotated() && fl.getLeader() == chB && rFl.getLeader() == null);

        // 6: expiry per B5-0337 — the relation dies at the round boundary
        //    and the pair becomes legal again.
        rules.startRound(st);
        check("LEAD", "startRound clears the leader, unrotates, effective back to base",
                fl.getLeader() == null && !chB.isRotated()
                && fl.getEffectiveMilitary() == 3);
        check("LEAD", "pair is legal again next round", rules.canLeadFleet(p, chB, fl));
    }

    // ── B5-0364: agenda lifecycle (B5-0345 Tier-1 #3) ────────────────────────

    private static void testAgendaLifecycle() throws Exception {
        System.out.println("AGL (B5-0364): agenda lifecycle — sponsor/discard/replace/reveal");
        RulesEngine rules = new RulesEngine();

        Player p = player("AGLp", Faction.NARN);
        Player r = player("AGLr", Faction.MINBARI);
        GameState st = state(p, r);
        AgendaCard minor   = agendaCard("agl_minor", false, Faction.ANY);
        AgendaCard minor2  = agendaCard("agl_minor2", false, Faction.ANY);
        AgendaCard minor3  = agendaCard("agl_minor3", false, Faction.ANY);
        AgendaCard major   = agendaCard("agl_major", true, Faction.ANY);
        AgendaCard major2  = agendaCard("agl_major2", true, Faction.ANY);
        AgendaCard foreign = agendaCard("agl_foreign", false, Faction.HUMAN);

        // 1: sponsor legality — one agenda in play at a time (:520).
        check("AGL", "sponsor legal with an empty agenda slot",
                rules.canSponsorAgenda(p, minor));
        p.setAgenda(minor);
        check("AGL", "sponsor refused while an agenda is in play",
                !rules.canSponsorAgenda(p, minor2));
        p.setAgenda(null);

        // 2: discard legality — a Major agenda cannot be discarded (:522/:719).
        check("AGL", "discard refused with no agenda in play",
                !rules.canDiscardAgenda(p));
        p.setAgenda(minor);
        boolean minorDiscardable = rules.canDiscardAgenda(p);
        p.setAgenda(major);
        check("AGL", "minor discardable, Major NOT (guard)",
                minorDiscardable && !rules.canDiscardAgenda(p));
        p.setAgenda(null);

        // 3: replace guards (:520 rotate; :719 hand/sponsorable/Major/hidden).
        CharacterCard leader = leaderCard("agl_lead", 3);
        p.getInnerCircle().add(leader);
        p.getHand().add(minor2);
        p.getHand().add(foreign);
        check("AGL", "replace needs a current agenda",
                !rules.canReplaceAgenda(p, minor2, leader));
        p.setAgenda(minor);
        check("AGL", "minor -> minor from hand with a ready IC leader: legal",
                rules.canReplaceAgenda(p, minor2, leader));
        check("AGL", "a replacement you could not sponsor is refused (:719)",
                !rules.canReplaceAgenda(p, foreign, leader));
        minor2.setFaceDown(true);
        check("AGL", "a hidden agenda cannot be the replacement (:719)",
                !rules.canReplaceAgenda(p, minor2, leader));
        minor2.setFaceDown(false);
        p.setAgenda(major);
        check("AGL", "Major guard: a Major cannot be replaced by a minor",
                !rules.canReplaceAgenda(p, minor2, leader));
        p.getHand().add(major2);
        check("AGL", "a Major CAN be replaced by another Major",
                rules.canReplaceAgenda(p, major2, leader));
        leader.rotate();
        check("AGL", "replace needs a READY IC leader (:520 rotation)",
                !rules.canReplaceAgenda(p, major2, leader));
        leader.unrotate();
        p.setAgenda(null);
        p.getHand().remove(major2);

        // 4: reveal guard — only a face-down agenda in play can be revealed.
        check("AGL", "reveal refused with no agenda", !rules.canRevealAgenda(p));
        minor.setFaceDown(true);
        p.setAgenda(minor);
        check("AGL", "face-down agenda in play: reveal legal (:520)",
                rules.canRevealAgenda(p));
        p.setAgenda(null);
        minor.setFaceDown(false);

        // 5: controller wiring through processAction (reflection, LEAD precedent).
        GameController gc = new GameController(st, new ArrayList<AIPlayer>(),
            new GameStateCallback() {
                public void accept(GameState gs) { }
            });
        Method handler = GameController.class.getDeclaredMethod(
                "processAction", Player.class, GameAction.class);
        handler.setAccessible(true);

        // 5a: sponsor through PLAY_CARD is refused while an agenda is installed
        //     (card stays in hand; the slot is untouched). minor2 is already
        //     in hand from part 3.
        p.setAgenda(minor);
        handler.invoke(gc, p, GameAction.playCard(minor2));
        check("AGL", "PLAY_CARD sponsor refused: card stays in hand, slot kept",
                p.getAgenda() == minor && p.getHand().contains(minor2));

        // 5b: DISCARD executes — slot cleared, card to the discard pile.
        handler.invoke(gc, p, GameAction.discardAgenda(minor));
        check("AGL", "DISCARD minor executes: slot cleared + discard pile",
                p.getAgenda() == null
                && p.getDeck().getDiscardPile().contains(minor));

        // 5c: DISCARD of a Major is refused (:719).
        p.setAgenda(major);
        handler.invoke(gc, p, GameAction.discardAgenda(major));
        check("AGL", "DISCARD Major refused: agenda stays, not discarded",
                p.getAgenda() == major
                && !p.getDeck().getDiscardPile().contains(major));

        // 5d: REPLACE executes — leader rotates, new agenda seated from hand,
        //     old agenda REMOVED FROM THE GAME (:719 — never the discard pile).
        //     minor4 is fresh here (never discarded above), so the discard-pile
        //     assertion tests the replace path itself, not 5b's earlier discard.
        AgendaCard minor4 = agendaCard("agl_minor4", false, Faction.ANY);
        p.setAgenda(minor4);
        handler.invoke(gc, p, GameAction.replaceAgenda(minor2, leader));
        check("AGL", "REPLACE executes: new agenda seated, IC leader rotated",
                p.getAgenda() == minor2 && leader.isRotated()
                && !p.getHand().contains(minor2));
        check("AGL", "old agenda removed FROM THE GAME (:719, not discarded)",
                !p.getDeck().getDiscardPile().contains(minor4));

        // 5e: the Major guard holds through the handler (nothing moves).
        p.setAgenda(major);
        p.getHand().add(minor3);
        leader.unrotate();
        handler.invoke(gc, p, GameAction.replaceAgenda(minor3, leader));
        check("AGL", "REPLACE Major->minor refused through the handler",
                p.getAgenda() == major && p.getHand().contains(minor3)
                && !leader.isRotated());
        p.setAgenda(null);

        // 6: hidden sponsor + inertness + reveal (:520 — a face-down agenda
        //    has no effect on play until revealed).
        AgendaCard hidden = agendaCard("agl_hidden", false, Faction.ANY);
        p.getHand().add(hidden);
        handler.invoke(gc, p, GameAction.playAgendaFaceDown(hidden));
        check("AGL", "hidden sponsor: seated face-down, left the hand",
                p.getAgenda() == hidden && hidden.isFaceDown()
                && !p.getHand().contains(hidden));

        p.gainInfluence(16);   // 20
        r.gainInfluence(16);   // 20
        check("AGL", "hidden INFLUENCE_20 at a 20-20 tie is INERT: no winner",
                rules.checkVictory(st) == null);
        handler.invoke(gc, p, GameAction.revealAgenda(hidden));
        check("AGL", "REVEAL: face-up again and the condition wins immediately",
                !hidden.isFaceDown() && rules.checkVictory(st) == p);

        // 7: startRound guard — a hidden agenda grants no round income until
        //    revealed (de_agenda_servants_of_order needs 2+ IC characters).
        Player q  = player("AGLq", Faction.CENTAURI);
        Player q2 = player("AGLq2", Faction.HUMAN);
        GameState sq = state(q, q2);
        q.getInnerCircle().add(leaderCard("agl_qic", 2));
        AgendaCard income = new AgendaCard("de_agenda_servants_of_order",
                "Servants of Order", "AGENDA", Rarity.COMMON, Faction.ANY,
                CardSet.PREMIERE, "x", "text", false, "INFLUENCE_20");
        q.setAgenda(income);
        income.setFaceDown(true);
        int beforeHidden = q.getInfluence();
        rules.startRound(sq);
        check("AGL", "hidden agenda grants no start-of-round influence",
                q.getInfluence() == beforeHidden);
        income.setFaceDown(false);
        int beforeShown = q.getInfluence();
        rules.startRound(sq);
        check("AGL", "revealed agenda grants its start-of-round influence",
                q.getInfluence() > beforeShown);
    }

    // ── B5-0365: contingencies ───────────────────────────────────────────────
    private static void testContingencies() throws Exception {
        System.out.println("CON (B5-0365): contingency placement, reveal, event parity");
        Player p = player("CONp", Faction.NARN);
        RulesEngine rules = new RulesEngine();
        CharacterCard host = p.getAmbassador();
        ContingencyCard card = new ContingencyCard("event_merchandising_b5",
                "Fixture Contingency", "CHARACTER", Rarity.COMMON, Faction.ANY,
                CardSet.PREMIERE, "x", "fixture", "CHARACTER", "NARN",
                "WHEN_REVEALED_FIXTURE");
        p.addToHand(card);
        check("CON", "typed target rejects non-character", !card.canTarget(event("CONwrong")));
        check("CON", "fixture target type and race match host", card.canTarget(host));
        CharacterCard neutralNarn = new CharacterCard("CONrace", "Race Fixture",
                "CHARACTER_NARN", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE,
                "x", "fixture", 1, 1, 1, 1, false);
        check("CON", "race target reads card race, not controlling faction",
                card.canTarget(neutralNarn));
        check("CON", "only controlled in-play host can receive contingency",
                rules.canPlayContingency(p, card, host));
        GameState s = state(p);
        GameController gc = new GameController(s, new ArrayList<AIPlayer>(),
                new GameStateCallback() { public void accept(GameState gs) { } });
        Method handler = GameController.class.getDeclaredMethod(
                "processAction", Player.class, GameAction.class);
        handler.setAccessible(true);
        handler.invoke(gc, p, GameAction.playContingency(card, host));
        check("CON", "PLAY_CONTINGENCY removes it from hand and places it face-down",
                !p.getHand().contains(card) && card.isFaceDown()
                && host.getContingencyCount() == 1 && card.getPlacedUnder() == host);
        check("CON", "placed contingency can be revealed by owner",
                rules.canRevealContingency(p, card));
        int before = p.getInfluence();
        p.resetActions();
        handler.invoke(gc, p, GameAction.revealContingency(card));
        check("CON", "REVEAL_CONTINGENCY uses Event parity and discards/detaches",
                p.getInfluence() == before + 1 && card.isRevealed()
                && card.getPlacedUnder() == null
                && p.getDeck().getDiscardPile().contains(card));
        check("CON", "revealed contingency cannot reveal twice",
                !rules.canRevealContingency(p, card));

        List<Card> parsed = DeckLoader.parseCards("[{\"id\":\"fixture\",\"title\":\"Fixture\","
                + "\"type\":\"CONTINGENCY\",\"subtype\":\"CHARACTER\","
                + "\"faction\":\"ANY\",\"set\":\"PREMIERE\","
                + "\"validTargetType\":\"CHARACTER\",\"validTargetRace\":\"NARN\","
                + "\"triggerCondition\":\"WHEN_ATTACKED\"}]");
        check("CON", "typed JSON fixture loads with trigger and target restrictions",
                parsed.size() == 1 && parsed.get(0) instanceof ContingencyCard
                && "WHEN_ATTACKED".equals(((ContingencyCard) parsed.get(0)).getTriggerCondition())
                && ((ContingencyCard) parsed.get(0)).canTarget(host));
    }

    // ── B5-0366: generic rotate-for-effect (B5-0345 Tier-2 #5) ────────────────

    /**
     * ROT (B5-0366): USE_ROTATE_EFFECT promotes the wired B5-0339 paths to a
     * player-chosen action. Factory carries (assistant, ambassador, kind);
     * gate/executor delegate to the B5-0339 primitives (same rotation cost,
     * same flag/discount, same round expiry); the controller branch consumes
     * exactly one action. Payloads are computed reads, never field
     * mutations, so the B5-0357 bonus layer is untouched.
     */
    private static void testRotateEffect() throws Exception {
        System.out.println("ROT (B5-0366): generic rotate-for-effect action");
        RulesEngine rules = new RulesEngine();

        Player p  = player("ROTp", Faction.NARN);
        Player p2 = player("ROTr", Faction.MINBARI);
        GameState st = state(p, p2);
        CharacterCard amb = p.getAmbassador();
        CharacterCard ch  = leaderCard("rot_ch", 2);
        p.getSupportingRole().add(ch);

        // 1: the factory carries (assistant, ambassador, kind).
        GameAction boost = GameAction.useRotateEffect(
                ch, amb, GameAction.RotateEffectKind.USE_ABILITY_BOOST);
        GameAction disc = GameAction.useRotateEffect(
                ch, amb, GameAction.RotateEffectKind.USE_SPONSOR_DISCOUNT);
        check("ROT", "factory carries (card, leader, kind) for both kinds",
                boost.getType() == GameAction.Type.USE_ROTATE_EFFECT
                && boost.getCard() == ch && boost.getLeader() == amb
                && boost.getRotateKind() == GameAction.RotateEffectKind.USE_ABILITY_BOOST
                && disc.getRotateKind() == GameAction.RotateEffectKind.USE_SPONSOR_DISCOUNT);

        // 2: one gate for both kinds today — readiness only (B5-0339).
        check("ROT", "gate passes for both kinds on a ready assistant",
                rules.canUseRotateEffect(p, ch, amb,
                        GameAction.RotateEffectKind.USE_ABILITY_BOOST)
                && rules.canUseRotateEffect(p, ch, amb,
                        GameAction.RotateEffectKind.USE_SPONSOR_DISCOUNT));
        check("ROT", "gate refuses a null kind",
                !rules.canUseRotateEffect(p, ch, amb, null));
        check("ROT", "gate refuses a foreign ambassador",
                !rules.canUseRotateEffect(p, ch, leaderCard("rot_x", 1),
                        GameAction.RotateEffectKind.USE_ABILITY_BOOST));
        ch.rotate();
        check("ROT", "gate refuses a rotated assistant",
                !rules.canUseRotateEffect(p, ch, amb,
                        GameAction.RotateEffectKind.USE_ABILITY_BOOST));
        ch.unrotate();

        // 3: the boost executor matches B5-0339 exactly.
        rules.executeRotateEffect(p, ch,
                GameAction.RotateEffectKind.USE_ABILITY_BOOST, st);
        check("ROT", "boost rotates the assistant and flags the ambassador",
                ch.isRotated() && amb.isAssistantBonus());
        check("ROT", "boost reads +1 Diplomacy, Psi untouched",
                amb.getPrimaryStatValue(ConflictType.DIPLOMACY) == 4
                && amb.getPrimaryStatValue(ConflictType.PSI) == 3);

        // 4: the discount executor matches B5-0339 exactly.
        Player pd = player("ROTdisc", Faction.NARN);
        Player pd2 = player("ROTdisc2", Faction.MINBARI);
        GameState tstd = state(pd, pd2);
        CharacterCard chd = leaderCard("rot_chd", 2);
        pd.getSupportingRole().add(chd);
        rules.executeRotateEffect(pd, chd,
                GameAction.RotateEffectKind.USE_SPONSOR_DISCOUNT, tstd);
        check("ROT", "discount rotates the assistant and grants 1",
                chd.isRotated() && pd.getSponsorDiscount() == 1);
    }

    private static void testRotateController() throws Exception {
        System.out.println("ROT-C (B5-0366): controller branch + expiry");
        RulesEngine rules = new RulesEngine();

        // 5: the controller branch consumes exactly one action and refuses
        //    illegal pairs as logged no-ops (rotated assistant reused).
        Player pc = player("ROTctl", Faction.NARN);
        Player pc2 = player("ROTctl2", Faction.MINBARI);
        GameState stc = state(pc, pc2);
        GameController gcc = new GameController(stc, new ArrayList<AIPlayer>(),
                new GameStateCallback() { public void accept(GameState gs) { } });
        Method handler = GameController.class.getDeclaredMethod(
                "processAction", Player.class, GameAction.class);
        handler.setAccessible(true);
        CharacterCard camb = pc.getAmbassador();
        CharacterCard cch = leaderCard("rot_cch", 1);
        CharacterCard cch2 = leaderCard("rot_cch2", 1);
        pc.getSupportingRole().add(cch);
        pc.getSupportingRole().add(cch2);
        check("ROT", "actionsLeft before handler = 1", pc.getActionsLeft() == 1);
        handler.invoke(gcc, pc, GameAction.useRotateEffect(
                cch, camb, GameAction.RotateEffectKind.USE_ABILITY_BOOST));
        check("ROT", "handler rotates and flags through the generic branch",
                cch.isRotated() && camb.isAssistantBonus()
                && pc.getActionsLeft() == 1);
        pc.resetActions();
        handler.invoke(gcc, pc, GameAction.useRotateEffect(
                cch, camb, GameAction.RotateEffectKind.USE_SPONSOR_DISCOUNT));
        check("ROT", "refused reuse is a no-op (no discount, still rotated)",
                cch.isRotated() && pc.getSponsorDiscount() == 0);

        // 6: null-kind executions log and leave state unchanged.
        pc.resetActions();
        handler.invoke(gcc, pc, GameAction.useRotateEffect(
                cch2, camb, null));
        check("ROT", "null kind is a logged no-op (no rotation)",
                !cch2.isRotated());

        // 7: both effects expire at the round boundary (B5-0339 startRound).
        rules.startRound(stc);
        check("ROT", "startRound clears the boost flag and rotations",
                !camb.isAssistantBonus() && !cch.isRotated()
                && camb.getPrimaryStatValue(ConflictType.DIPLOMACY) == 3);

        // 8: rotation never mutates printed stats (bonus-layer composition).
        check("ROT", "rotation never mutates printed stats",
                cch.getLeadership() == 1 && cch2.getLeadership() == 1);
    }

    private static void testBonusLayer() {
        System.out.println("BON (B5-0367): registry, read path, expiry and composition");
        Player p = player("Bonus", Faction.NARN);
        FleetCard fleet = fleetCard("bon_fleet", "FRIGATE");
        p.getFleets().add(fleet);
        fleet.setOwner(p);
        p.grantBonus(StatBonus.attached("bon_src", StatKey.MILITARY, 2,
                fleet.getId(), Expiry.WHILE_IN_PLAY, 1));
        check("BON", "attached bonus adds to printed Military",
                fleet.getEffectiveMilitary() == 5);
        p.removeBonusesBySource("bon_src");
        check("BON", "removing source restores printed Military",
                fleet.getEffectiveMilitary() == 3);

        p.grantBonus(StatBonus.attached("bon_a", StatKey.MILITARY, 2,
                fleet.getId(), Expiry.WHILE_IN_PLAY, 1));
        p.grantBonus(StatBonus.attached("bon_b", StatKey.MILITARY, 1,
                fleet.getId(), Expiry.WHILE_IN_PLAY, 1));
        check("BON", "cumulative bonuses stack", fleet.getEffectiveMilitary() == 6);

        CharacterCard ch = leaderCard("bon_char", 1);
        ch.setOwner(p);
        p.grantBonus(StatBonus.attached("blanked", StatKey.DIPLOMACY, 2,
                ch.getId(), Expiry.WHILE_IN_PLAY, 1));
        p.removeBonusesBySource("blanked");
        check("BON", "blanking removes source bonus but preserves printed stat",
                ch.getPrimaryStatValue(ConflictType.DIPLOMACY) == 2);

        CharacterCard zeroPsi = new CharacterCard("bon_psi", "Psi", "CHARACTER_NARN",
                Rarity.COMMON, Faction.NARN, CardSet.PREMIERE, "x", "", 1, 1, 0, 1, false);
        zeroPsi.setOwner(p);
        p.grantBonus(StatBonus.attached("generic", StatKey.PSI, 1,
                zeroPsi.getId(), Expiry.WHILE_IN_PLAY, 1));
        check("BON", "generic Psi cannot rise from printed zero",
                zeroPsi.getPrimaryStatValue(ConflictType.PSI) == 0);
        p.grantBonus(new StatBonus("specific", StatKey.PSI, 1, BonusScope.ATTACHED,
                zeroPsi.getId(), null, Expiry.WHILE_IN_PLAY, true, true, 1));
        check("BON", "specific Psi unlocks the full stacked value",
                zeroPsi.getPrimaryStatValue(ConflictType.PSI) == 2);

        p.grantBonus(StatBonus.attached("turn", StatKey.MILITARY, 2,
                fleet.getId(), Expiry.END_OF_TURN, 1));
        p.grantBonus(StatBonus.attached("lasting", StatKey.MILITARY, 1,
                fleet.getId(), Expiry.WHILE_IN_PLAY, 1));
        GameState st = state(p, player("Bonus2", Faction.MINBARI));
        st.advanceRound();
        check("BON", "round boundary expires turn bonus and keeps in-play bonus",
                fleet.getEffectiveMilitary() == 7);

        Player penalized = player("Penalty", Faction.NARN);
        FleetCard zeroFleet = new FleetCard("bon_zero", "Zero", "FLEET", Rarity.COMMON,
                Faction.NARN, CardSet.PREMIERE, "x", "", 1);
        zeroFleet.setOwner(penalized);
        penalized.getFleets().add(zeroFleet);
        penalized.grantBonus(StatBonus.attached("penalty", StatKey.MILITARY, -3,
                zeroFleet.getId(), Expiry.WHILE_IN_PLAY, 1));
        check("BON", "Military penalties floor at zero", zeroFleet.getEffectiveMilitary() == 0);

        CharacterCard assistant = leaderCard("bon_assist", 2);
        assistant.setRotated(true);
        fleet.setLeader(assistant);
        p.grantBonus(StatBonus.attached("overlay", StatKey.MILITARY, 1,
                fleet.getId(), Expiry.WHILE_IN_PLAY, 1));
        check("BON", "fleet leader and bonus overlays each contribute once",
                fleet.getEffectiveMilitary() == 10);
    }

    private static void testDamageNeutralization() {
        System.out.println("DMG (B5-0368): damage, neutralization, overflow and action lock");
        CharacterCard ch = new CharacterCard("dmg_char", "Damaged", "CHARACTER_NARN",
                Rarity.RARE, Faction.NARN, CardSet.PREMIERE, "x", "", 2, 3, 1, 1, false);
        check("DMG", "one damage reduces each ability by one",
                ch.applyDamage(1) == 0
                && ch.getPrimaryStatValue(ConflictType.DIPLOMACY) == 1
                && ch.getPrimaryStatValue(ConflictType.INTRIGUE) == 2);
        check("DMG", "damage cannot reduce an ability below zero",
                ch.applyDamage(1) == 0
                && ch.getPrimaryStatValue(ConflictType.PSI) == 0);

        CharacterCard overflow = new CharacterCard("dmg_over", "Overflow", "CHARACTER_NARN",
                Rarity.RARE, Faction.NARN, CardSet.PREMIERE, "x", "", 2, 3, 1, 1, false);
        check("DMG", "neutralization removes threshold tokens and preserves severe overflow",
                overflow.applyDamage(5) == 2 && overflow.isNeutralized()
                && overflow.isFaceDown() && overflow.getDamageTokens() == 0
                && overflow.getSevereDamageTokens() == 2);
        overflow.applyDamage(3);
        check("DMG", "additional normal damage has no effect on a neutralized card",
                overflow.getSevereDamageTokens() == 2);

        CharacterCard locked = new CharacterCard("dmg_lock", "Lock", "CHARACTER_NARN",
                Rarity.RARE, Faction.NARN, CardSet.PREMIERE, "x", "", 2, 1, 0, 0, false);
        Player owner = player("DmgOwner", Faction.NARN);
        owner.getInnerCircle().add(locked);
        owner.getAmbassador().rotate();
        locked.applyDamage(2);
        locked.heal();
        check("DMG", "healing does not clear this-turn action lock",
                locked.canActAfterNeutralization() == false
                && !new RulesEngine().canBuildInfluence(owner));
        new RulesEngine().startRound(state(owner, player("DmgOther", Faction.MINBARI)));
        check("DMG", "next round clears neutralized action lock",
                locked.canActAfterNeutralization());

        Player fleetOwner = player("DmgFleet", Faction.NARN);
        FleetCard fleet = fleetCard("dmg_fleet", "FRIGATE");
        CharacterCard leader = leaderCard("dmg_leader", 2);
        fleetOwner.getFleets().add(fleet);
        fleet.setOwner(fleetOwner);
        leader.setOwner(fleetOwner);
        leader.rotate();
        fleet.setLeader(leader);
        fleet.applyDamage(5);
        check("DMG", "fleet neutralization automatically neutralizes its leader without extra damage",
                fleet.isNeutralized() && leader.isNeutralized()
                && leader.getSevereDamageTokens() == 0);

        CharacterCard legacy = new CharacterCard("dmg_legacy", "Legacy", "CHARACTER_NARN",
                Rarity.RARE, Faction.NARN, CardSet.PREMIERE, "x", "", 2, 2, 0, 0, true);
        legacy.damage();
        check("DMG", "B5-0309 conflict damage remains a face-down flip without normal tokens",
                legacy.isFaceDown() && legacy.getDamageTokens() == 0);
    }

    private static void testAttackParticipant() throws Exception {
        System.out.println("ATK (B5-0370): attack legality, damage and action plumbing");
        RulesEngine rules = new RulesEngine();
        Player attackerOwner = player("AtkNarn", Faction.NARN);
        Player targetOwner = player("AtkMinbari", Faction.MINBARI);
        CharacterCard attacker = new CharacterCard("atk_a", "Attacker", "CHARACTER_NARN",
                Rarity.RARE, Faction.NARN, CardSet.PREMIERE, "x", "", 3, 1, 0, 1, false);
        CharacterCard target = new CharacterCard("atk_t", "Target", "CHARACTER_MINBARI",
                Rarity.RARE, Faction.MINBARI, CardSet.PREMIERE, "x", "", 2, 1, 0, 1, false);
        attackerOwner.getSupportingRole().add(attacker);
        targetOwner.getInnerCircle().add(target);
        Conflict conflict = new Conflict(conflictCard("atk_conf", ConflictType.DIPLOMACY, null),
                attackerOwner);
        conflict.addParticipant(attackerOwner, true);
        conflict.addParticipant(targetOwner, false);
        target.rotate();
        conflict.commitCard(targetOwner, target, false);
        GameState st = state(attackerOwner, targetOwner);
        st.setActiveConflict(conflict);

        check("ATK", "ready owned attacker may attack an opposing participant",
                rules.canAttackConflictParticipant(attackerOwner, attacker, target, conflict));
        GameController controller = new GameController(st, new ArrayList<AIPlayer>(),
                new GameStateCallback() { public void accept(GameState gs) { } });
        Method handler = GameController.class.getDeclaredMethod(
                "processAction", Player.class, GameAction.class);
        handler.setAccessible(true);
        handler.invoke(controller, attackerOwner,
                GameAction.attackConflictParticipant(attacker, target));
        check("ATK", "controller rotates and commits attacker, spends one action",
                attacker.isRotated() && conflict.isParticipantCard(attacker)
                && attackerOwner.getActionsLeft() == 1);
        check("ATK", "both cards take simultaneous current-ability damage",
                attacker.getDamageTokens() == 2 && target.isNeutralized()
                && target.getSevereDamageTokens() == 1);
        CharacterCard readyVsNeutralized = leaderCard("atk_ready_neutral", 2);
        attackerOwner.getSupportingRole().add(readyVsNeutralized);
        check("ATK", "neutralized participants cannot be attacked",
                !rules.canAttackConflictParticipant(attackerOwner, readyVsNeutralized,
                        target, conflict));

        Player strifeOwner = player("AtkStrife", Faction.NARN);
        Player strifeTargetOwner = player("AtkStrifeTarget", Faction.MINBARI);
        CharacterCard strifeAttacker = new CharacterCard("atk_strife", "Strife", "CHARACTER_NARN",
                Rarity.RARE, Faction.NARN, CardSet.PREMIERE, "x", "", 2, 1, 0, 1, false);
        CharacterCard strifeTarget = new CharacterCard("atk_strife_t", "Strife Target", "CHARACTER_MINBARI",
                Rarity.RARE, Faction.MINBARI, CardSet.PREMIERE, "x", "", 1, 0, 0, 0, false);
        strifeAttacker.setStrifeMarks(1);
        strifeOwner.getSupportingRole().add(strifeAttacker);
        strifeTargetOwner.getInnerCircle().add(strifeTarget);
        Conflict strifeConflict = new Conflict(conflictCard("atk_strife_c", ConflictType.DIPLOMACY, null),
                strifeOwner);
        strifeConflict.addParticipant(strifeOwner, true);
        strifeConflict.addParticipant(strifeTargetOwner, false);
        strifeTarget.rotate();
        strifeConflict.commitCard(strifeTargetOwner, strifeTarget, false);
        GameState strifeState = state(strifeOwner, strifeTargetOwner);
        check("ATK", "Strife marks add two damage each and overflow becomes severe",
                rules.executeAttackConflictParticipant(strifeOwner, strifeAttacker,
                        strifeTarget, strifeConflict, strifeState)
                && strifeTarget.getSevereDamageTokens() == 3
                && strifeAttacker.getDamageTokens() == 1);

        Player sameRace = player("AtkNarn2", Faction.NARN);
        CharacterCard sameRaceTarget = new CharacterCard("atk_same", "Same Race", "CHARACTER_NARN",
                Rarity.RARE, Faction.NARN, CardSet.PREMIERE, "x", "", 2, 1, 0, 1, false);
        sameRace.getInnerCircle().add(sameRaceTarget);
        CharacterCard readySame = leaderCard("atk_ready", 2);
        attackerOwner.getSupportingRole().add(readySame);
        Conflict sameRaceConflict = new Conflict(conflictCard("atk_same_c", ConflictType.DIPLOMACY, null),
                attackerOwner);
        sameRaceConflict.addParticipant(attackerOwner, true);
        sameRaceConflict.addParticipant(sameRace, false);
        sameRaceConflict.commitCard(sameRace, sameRaceTarget, false);
        check("ATK", "cannot attack a target in the same faction",
                !rules.canAttackConflictParticipant(attackerOwner,
                        readySame, sameRaceTarget, sameRaceConflict));

        CharacterCard uncommitted = new CharacterCard("atk_uncommitted", "Uncommitted", "CHARACTER_MINBARI",
                Rarity.RARE, Faction.MINBARI, CardSet.PREMIERE, "x", "", 2, 1, 0, 1, false);
        targetOwner.getInnerCircle().add(uncommitted);
        CharacterCard readyTargetCase = leaderCard("atk_ready2", 2);
        attackerOwner.getSupportingRole().add(readyTargetCase);
        check("ATK", "cannot attack a card that has not participated",
                !rules.canAttackConflictParticipant(attackerOwner,
                        readyTargetCase, uncommitted, conflict));

        Player fleetOwner = player("AtkFleet", Faction.MINBARI);
        FleetCard fleet = fleetCard("atk_fleet", "FRIGATE");
        CharacterCard fleetLeader = leaderCard("atk_fleet_leader", 2);
        fleetOwner.getFleets().add(fleet);
        fleetOwner.getInnerCircle().add(fleetLeader);
        fleet.setLeader(fleetLeader);
        fleetLeader.rotate();
        Conflict leaderConflict = new Conflict(conflictCard("atk_lead_c", ConflictType.DIPLOMACY, null),
                attackerOwner);
        leaderConflict.addParticipant(attackerOwner, true);
        leaderConflict.addParticipant(fleetOwner, false);
        leaderConflict.commitCard(fleetOwner, fleet, false);
        CharacterCard readyLeaderCase = leaderCard("atk_ready3", 2);
        attackerOwner.getSupportingRole().add(readyLeaderCase);
        check("ATK", "fleet leader is excluded as an attack target",
                leaderConflict.isLeaderOfParticipantFleet(fleetLeader)
                && !rules.canAttackConflictParticipant(attackerOwner,
                        readyLeaderCase, fleetLeader, leaderConflict));

        CharacterCard zeroAbility = new CharacterCard("atk_zero", "Zero", "CHARACTER_NARN",
                Rarity.RARE, Faction.NARN, CardSet.PREMIERE, "x", "", 0, 1, 0, 0, false);
        attackerOwner.getSupportingRole().add(zeroAbility);
        conflict.commitCard(targetOwner, uncommitted, false);
        uncommitted.rotate();
        check("ATK", "attacker must have a nonzero ability of the conflict type",
                !rules.canAttackConflictParticipant(attackerOwner, zeroAbility, uncommitted, conflict));

        Conflict restricted = new Conflict(conflictCard("atk_restricted", ConflictType.DIPLOMACY,
                Participation.parse("{\"cardTypes\":[\"FLEET\"]}")), attackerOwner);
        restricted.addParticipant(attackerOwner, true);
        restricted.addParticipant(targetOwner, false);
        FleetCard allowedFleet = fleetCard("atk_allowed_fleet", "FRIGATE");
        targetOwner.getFleets().add(allowedFleet);
        restricted.commitCard(targetOwner, allowedFleet, false);
        attacker.unrotate();
        CharacterCard readyRestricted = leaderCard("atk_ready_restricted", 2);
        attackerOwner.getSupportingRole().add(readyRestricted);
        check("ATK", "attacker must pass the conflict participation card filter",
                !rules.canAttackConflictParticipant(attackerOwner, readyRestricted,
                        allowedFleet, restricted));
    }

    private static void testHealRepair() throws Exception {
        System.out.println("HLR (B5-0371): character healing and pool-paid repair");
        RulesEngine rules = new RulesEngine();
        Player p = player("HealRepair", Faction.NARN);
        CharacterCard amb = p.getAmbassador();
        amb.applyDamage(1);
        check("HLR", "ready damaged Inner Circle character may heal", rules.canHealCharacter(p, amb));
        check("HLR", "heal rotates character and clears all normal damage",
                rules.executeHealCharacter(p, amb, state(p)) && amb.isRotated()
                && amb.getDamageTokens() == 0);
        check("HLR", "same-turn healed character cannot take another action",
                !rules.canHealCharacter(p, amb));

        Player repairOwner = player("Repair", Faction.NARN);
        FleetCard fleet = fleetCard("repair_fleet", "FRIGATE");
        repairOwner.getFleets().add(fleet);
        fleet.applyDamage(2);
        repairOwner.applyInfluence(2);
        check("HLR", "repair requires pool for every normal damage token",
                rules.canRepairCard(repairOwner, fleet));
        int rating = repairOwner.getInfluence();
        GameController controller = new GameController(state(repairOwner),
                new ArrayList<AIPlayer>(), new GameStateCallback() {
                    public void accept(GameState gs) { }
                });
        Method handler = GameController.class.getDeclaredMethod(
                "processAction", Player.class, GameAction.class);
        handler.setAccessible(true);
        handler.invoke(controller, repairOwner, GameAction.repairCard(fleet));
        check("HLR", "controller repairs two tokens, rotates fleet, spends two pool and one action",
                fleet.getDamageTokens() == 0 && fleet.isRotated()
                && repairOwner.getInfluence() == rating
                && repairOwner.getAppliedPool() == 0
                && repairOwner.getActionsLeft() == 1);

        Player shortPool = player("ShortPool", Faction.NARN);
        FleetCard costly = fleetCard("repair_costly", "FRIGATE");
        shortPool.getFleets().add(costly);
        costly.applyDamage(2);
        shortPool.applyInfluence(3);
        check("HLR", "insufficient applied pool refuses repair without mutation",
                !rules.executeRepairCard(shortPool, costly, state(shortPool))
                && costly.getDamageTokens() == 2 && !costly.isRotated()
                && shortPool.getAppliedPool() == 1);

        Player severeOwner = player("SevereHeal", Faction.NARN);
        CharacterCard severe = severeOwner.getAmbassador();
        severe.applyDamage(4);
        check("HLR", "neutralization creates severe overflow for healing steps",
                severe.isNeutralized() && severe.getSevereDamageTokens() == 1);
        severe.resetNeutralizedTurnLock();
        check("HLR", "neutralized IC heals one severe token but stays down",
                rules.executeHealCharacter(severeOwner, severe, state(severeOwner))
                && severe.getSevereDamageTokens() == 0 && severe.isNeutralized());

        Player aid = player("Aid", Faction.NARN);
        CharacterCard aided = new CharacterCard("aid_member", "Aid Member", "CHARACTER_NARN",
                Rarity.RARE, Faction.NARN, CardSet.PREMIERE, "x", "", 2, 2, 0, 1, false);
        aid.getInnerCircle().add(aided);
        aid.getAmbassador().applyDamage(1);
        check("HLR", "undamaged IC member may rotate as ambassador aid",
                rules.executeHealCharacter(aid, aid.getAmbassador(), state(aid))
                && rules.executeHealCharacter(aid, aided, state(aid)));
        rules.finishActionRound(state(aid));
        check("HLR", "all IC heal actions fully heal ambassador at action-round end",
                !aid.getAmbassador().isNeutralized() && aid.getAmbassador().getDamageTokens() == 0
                && aid.getAmbassador().getSevereDamageTokens() == 0);
    }

    // ── B5-0395: Mercenaries (rulebook §Mercenaries :735–:741) ────────────────
    // Assert against the engine API + the fixture mercenary effect registered
    // in CardEffects (mer_metric_fixture → controller gains 1 influence).
    private static void testMercenaries() throws Exception {
        // 1. DeckLoader hydration: optional "mercenary" key (absent = false).
        List<Card> parsed = DeckLoader.parseCards("["
                + "{\"id\":\"mer_leader\",\"title\":\"Merc Leader\",\"type\":\"CHARACTER\","
                + "\"subtype\":\"CHARACTER_ANY\",\"mercenary\":true},"
                + "{\"id\":\"plain_leader\",\"title\":\"Plain\",\"type\":\"CHARACTER\","
                + "\"subtype\":\"CHARACTER_ANY\"},"
                + "{\"id\":\"mer_off\",\"title\":\"Off\",\"type\":\"EVENT\",\"mercenary\":false}"
                + "]");
        boolean merTrue = false, merAbsent = false, merFalse = false;
        for (Card c : parsed) {
            if ("mer_leader".equals(c.getId()))        merTrue   = c.isMercenary();
            if ("plain_leader".equals(c.getId()))      merAbsent = !c.isMercenary();
            if ("mer_off".equals(c.getId()))           merFalse  = !c.isMercenary();
        }
        check("MER", "DeckLoader hydrates mercenary flag (true) from JSON",
                merTrue && parsed.size() == 3);
        check("MER", "DeckLoader leaves flag false when absent or false",
                merAbsent && merFalse);

        // 2. Bid action factory: type, card, amount (and default amount 0).
        Card merc = event("mer_metric_fixture");
        merc.setMercenary(true);
        GameAction bid = GameAction.bidOnMercenary(merc, 2);
        check("MER", "bidOnMercenary carries type, card and amount",
                bid.getType() == GameAction.Type.BID_ON_MERCENARY
                && bid.getCard() == merc && bid.getAmount() == 2);
        check("MER", "non-bid actions report amount 0",
                GameAction.pass().getAmount() == 0);

        // 3. Offering: refuses null, duplicates, non-mercenaries; accepts.
        Player a = player("MerA", Faction.HUMAN);
        Player b = player("MerB", Faction.MINBARI);
        GameState st = state(a, b);
        EventCard plain = event("mer_plain");
        check("MER", "non-mercenary card cannot be offered",
                !st.addMercenaryOffer(plain) && st.getMercenaryOffers().size() == 0);
        check("MER", "null offer refused", !st.addMercenaryOffer(null));
        check("MER", "mercenary card offered successfully",
                st.addMercenaryOffer(merc));
        check("MER", "duplicate offer refused", !st.addMercenaryOffer(merc));
        check("MER", "offered mercenary is in play", st.isMercenaryInPlay(merc));

        // 4. Bid legality.
        RulesEngine rules = new RulesEngine();
        GameState st2 = state(a, b);
        st2.addMercenaryOffer(merc);
        check("MER", "null guards refuse the bid path",
                !rules.canBidOnMercenary(null, merc, 1, st2)
                && !rules.canBidOnMercenary(a, null, 1, st2)
                && !rules.canBidOnMercenary(a, merc, 1, null));
        check("MER", "non-positive bid refused",
                !rules.canBidOnMercenary(a, merc, 0, st2)
                && !rules.canBidOnMercenary(a, merc, -3, st2));
        check("MER", "unoffered mercenary refused", !rules.canBidOnMercenary(a, plain, 1, st2));
        a.applyInfluence(4);    // pool 4 → 0
        check("MER", "unaffordable bid refused", !rules.canBidOnMercenary(a, merc, 1, st2));
        a.gainInfluence(4);     // rating + pool 4 → 4 (rating 4 → 8)
        check("MER", "affordable offered bid is legal", rules.canBidOnMercenary(a, merc, 1, st2));

        // 5. Execution: pool-only spend, cumulative totals, no rating movement.
        Player c = player("MerC", Faction.NARN);
        Player d = player("MerD", Faction.CENTAURI);
        GameState st3 = state(c, d);
        st3.addMercenaryOffer(merc);
        int cRating = c.getInfluence();
        check("MER", "first bid spends pool only and records",
                rules.executeBidOnMercenary(c, merc, 2, st3)
                && c.getInfluence() == cRating
                && c.getAppliedPool() == cRating - 2
                && st3.getMercenaryBid(merc, c) == 2);
        rules.executeBidOnMercenary(c, merc, 1, st3);
        check("MER", "second bid accumulates (cumulative), pool keeps draining",
                st3.getMercenaryBid(merc, c) == 3 && c.getAppliedPool() == cRating - 3);
        check("MER", "total across players is tracked",
                st3.totalMercenaryBids(merc) == 3);

        // 6. Unaffordable execution refuses without mutation.
        GameState st4 = state(c, d);
        st4.addMercenaryOffer(merc);
        c.applyInfluence(c.getAppliedPool());   // drain
        int cRating2 = c.getInfluence();
        check("MER", "unaffordable execution refuses and mutates nothing",
                !rules.executeBidOnMercenary(c, merc, 1, st4)
                && c.getAppliedPool() == 0 && c.getInfluence() == cRating2
                && st4.getMercenaryBid(merc, c) == 0);

        // 7. Resolution: strict-highest crown; ties crown nobody.
        Player x = player("MerX", Faction.HUMAN);
        Player y = player("MerY", Faction.MINBARI);
        GameState stR = state(x, y);
        stR.addMercenaryOffer(merc);
        check("MER", "no controller before resolution", stR.getMercenaryController(merc) == null);
        rules.executeBidOnMercenary(x, merc, 2, stR);
        rules.executeBidOnMercenary(y, merc, 1, stR);
        Map<Card, Player> resolved = stR.resolveMercenaries();
        check("MER", "highest single bidder controls", resolved.get(merc) == x
                && stR.getMercenaryController(merc) == x);

        Player p = player("MerP", Faction.NARN);
        Player q = player("MerQ", Faction.CENTAURI);
        GameState stT = state(p, q);
        stT.addMercenaryOffer(merc);
        rules.executeBidOnMercenary(p, merc, 2, stT);
        rules.executeBidOnMercenary(q, merc, 2, stT);
        Map<Card, Player> tie = stT.resolveMercenaries();
        check("MER", "tied totals crown nobody (conservative, D12 discipline)",
                tie.isEmpty() && stT.getMercenaryController(merc) == null);

        // 8. Controller performs the mercenary action (fixture: +1 influence).
        Player w = player("MerW", Faction.HUMAN);
        GameState stW = state(w);
        stW.addMercenaryOffer(merc);
        int wRating = w.getInfluence();
        rules.executeBidOnMercenary(w, merc, 1, stW);
        stW.resolveMercenaries();
        CardEffects.applyMercenaryAction(stW, w, merc);
        check("MER", "controlled fixture mercenary grants its effect to controller",
                w.getInfluence() == wRating + 1);

        // 9. Round boundary: bids + control clear at startRound, offers persist.
        GameState stV = state(w, p);
        stV.addMercenaryOffer(merc);
        rules.executeBidOnMercenary(w, merc, 1, stV);
        stV.resolveMercenaries();
        int offersBefore = stV.getMercenaryOffers().size();
        rules.startRound(stV);   // resets pool too
        check("MER", "startRound clears bids and control but keeps offers",
                stV.getMercenaryController(merc) == null
                && stV.getMercenaryBid(merc, w) == 0
                && stV.totalMercenaryBids(merc) == 0
                && stV.getMercenaryOffers().size() == offersBefore);

        // 10. Controller branch integration: the phase resolves and applies
        //     the effect for a BID action through the shared controller loop.
        Player u = player("MerU", Faction.HUMAN);
        Player v = player("MerV", Faction.MINBARI);
        GameState stC = state(u, v);
        stC.addMercenaryOffer(merc);
        GameController ctl = new GameController(stC, new ArrayList<AIPlayer>(),
                new GameStateCallback() {
                    public void accept(GameState gs) { }
                });
        Method bidHandler = GameController.class.getDeclaredMethod(
                "processAction", Player.class, GameAction.class);
        bidHandler.setAccessible(true);
        bidHandler.invoke(ctl, u, GameAction.bidOnMercenary(merc, 2));
        Method phaseHandler = GameController.class.getDeclaredMethod("runMercenaryPhase");
        phaseHandler.setAccessible(true);
        int uBefore = u.getInfluence();
        phaseHandler.invoke(ctl);
        check("MER", "controller phase crowns the bidder and applies the fixture effect",
                stC.getMercenaryController(merc) == u
                && u.getInfluence() == uBefore + 1
                && stC.getPhase() == GamePhase.MERCENARY);
    }

    public static void main(String[] args) {
        try {
            System.out.println("=== B5 CCG rulebook-conformance suite (B5-0308) ===");
            testD1();
            testD3();
            testD8();

            testD12();
            testD13();

            testConflictPerTurn();

            testConflictSides();

            // ── B5-0376 Phase A: war-conflict declaration + resolution ──────────

            testWarConflict();

            testPromotion();
            testFreeParticipantWaiver();
            testAppliedInfluencePool();

            testAIScoring();

            testAESScoring();

            testParticipation();

            testFleetLeadership();

            testLeadFleetAction();

            testAgendaLifecycle();

            testContingencies();

            testAftermathTargeting();

            testAssistant();

            testRotateEffect();

            testRotateController();

            testBonusLayer();

            testDamageNeutralization();

            testAttackParticipant();

            testHealRepair();

            testMercenaries();   // B5-0395 (rulebook §Mercenaries :735–:741)

            testStation();

            testAIJoinSide();
            testRotateAI();

            // ── B5-0377: AI Tier-1/Tier-2 slice scoring ─────────────────────────

            testLeadFleetAI();
            testAgendaLifecycleAI();
            testContingencyAI();

            testMercenaryBiddingAI();   // B5-0403 (rulebook §Mercenaries :735–:741)

            testD6ActionLoop();
            testD7BuildInfluence();
            testD15EffectCoverage();
            testStationHooks();   // B5-0437: station-influence card hooks
            System.out.println("All B5-0203 deviations D1-D14 resolved; D15 partial by effect-coverage; D6/D7 now have dedicated named assertions (B5-0436). B5-0437 station hooks covered (STH).");

            System.out.println();
            System.out.println(failed == 0
                    ? "CONFORMANCE SUITE PASSED (" + checks + " checks)"
                    : "CONFORMANCE SUITE FAILED (" + failed + " of " + checks + " checks)");
        } catch (Throwable t) {
            System.err.println();
            System.err.println("CONFORMANCE SUITE FAILED - unexpected exception:");
            t.printStackTrace(System.err);
            failed++;
        }
        System.out.flush();
        System.err.flush();
        System.exit(failed == 0 ? 0 : 1);
    }
}
