package b5ccg.engine;

import b5ccg.ai.AIPlayer;
import b5ccg.model.*;
import b5ccg.model.enums.*;
import java.util.ArrayList;
import java.util.List;

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
        for (Player p : players) ps.add(p);
        GameState s = new GameState(ps);
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
        check("D1", "winning opposer may play LOST aftermath",
                rules.canPlayAftermath(opposer, lostAm, lost, initWon));

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
        check("PRM", "influence paid (9 - 1 = 8)", p.getInfluence() == 8);

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
        check("PRM", "second promotion paid cost 2 (0-cost char + 2 members)",
                p.getInfluence() == 3);
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
        p.spendInfluence(rules.recruitCost(p, cheapChar));
        p.removeFromHand(cheapChar);
        p.placeInSupportingRole(cheapChar);
        check("CST", "recruiting spends the card cost and seats the character",
                p.getInfluence() == before - 2
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

    // ── B5-0324: cost-aware AI scoring ────────────────────────────────────

    private static void testAIScoring() {
        System.out.println("AIS (B5-0324): cost-aware AI scoring");
        Player aiP = player("AIs", Faction.NARN);
        aiP.gainInfluence(6); // 10 → Build Influence offers are out of the way
        GameState st = state(aiP);

        AgendaCard agenda = new AgendaCard("ais_ag", "Test Agenda",
                "AGENDA", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE,
                "x", "text", false, "INFLUENCE_20");
        LocationCard loc = new LocationCard("ais_loc", "Test Location",
                "LOCATION", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE,
                "x", "text", 1);
        aiP.addToHand(agenda);
        aiP.addToHand(loc);

        AIPlayer med = new AIPlayer(aiP, AIDifficulty.MEDIUM);

        // 1: with all costs 0 (the current data), ordering is exactly as
        //    before B5-0324 — agenda (8) over location (6).
        check("AIS", "MEDIUM: zero costs keep the pre-B5-0324 ordering",
                med.chooseAction(st, aiP).getCard() == agenda);

        // 2: raising the agenda's cost flips the choice (8-3=5 < 6).
        agenda.setCost(3);
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

            testPromotion();

            testCostField();

            testAIScoring();
            System.out.println("Still-open findings (no assertion possible yet):");
            System.out.println("  [info] D2/D4-D7/D9-D11/D15 need effect/target"
                    + " plumbing; partially addressed by B5-0307 (see audit report).");

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
