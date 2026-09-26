package b5ccg.engine;

import b5ccg.model.*;
import b5ccg.model.enums.*;
import java.util.*;

/**
 * Central per-card effect dispatch. Keys effects on the card id exactly as it
 * appears in the card JSON (b5ccg/resources/cards/*.json) — no card-text
 * parsing. Every entry is data-verified: ids come from the JSON, enhancement
 * magnitudes come from the JSON bonus fields, and event/agenda/conflict
 * magnitudes come from the card text stored in that JSON.
 *
 * Wiring (GameController / RulesEngine):
 *   - Events:       applyPlayEvent replaces the generic "draw 1" in
 *                   applyGenericCardPlay; unknown ids keep that behaviour.
 *   - Enhancements: applyPlayEnhancement attaches to a live target and applies
 *                   the JSON bonus fields via CharacterCard.applyStatDelta /
 *                   FleetCard.applyMilitaryDelta (existing model methods).
 *   - Conflicts:    applyConflictOutcome runs loser penalties after the
 *                   winner's influence reward; winner reward stays in
 *                   RulesEngine.resolveConflict.
 *   - Agendas:      applyAgendaStartOfRound (RulesEngine.startRound),
 *                   applyAgendaOnPlay + applyAgendaDiplomacyWin
 *                   (GameController).
 *
 * The model interface CardEffect remains declared; the id-keyed tables below
 * are its implementation. Java 6 only: no lambdas, method refs, streams,
 * diamonds, or try-with-resources.
 */
public final class CardEffects {

    private CardEffects() { }

    /** Typed free-action waivers dispatched by card id, without text parsing. */
    public enum WaiverEffect {
        NONE, FREE_SPONSOR, FREE_PARTICIPANT
    }

    private static final Map<String, WaiverEffect> WAIVER_EFFECTS =
            new HashMap<String, WaiverEffect>();
    static {
        // B5-0360 E3 / Q4: both set versions grant the same join waiver.
        WAIVER_EFFECTS.put("conf_non_aligned_support", WaiverEffect.FREE_PARTICIPANT);
        WAIVER_EFFECTS.put("de_conf_non_aligned_support", WaiverEffect.FREE_PARTICIPANT);
    }

    public static WaiverEffect sponsorWaiver(Card card) {
        if (card == null) return WaiverEffect.NONE;
        WaiverEffect effect = WAIVER_EFFECTS.get(card.getId());
        return effect == WaiverEffect.FREE_SPONSOR ? effect : WaiverEffect.NONE;
    }

    public static WaiverEffect participantWaiver(Conflict conflict) {
        if (conflict == null) return WaiverEffect.NONE;
        WaiverEffect effect = WAIVER_EFFECTS.get(conflict.getCard().getId());
        return effect == WaiverEffect.FREE_PARTICIPANT ? effect : WaiverEffect.NONE;
    }

    // ── Events: card id → influence gain for the playing player ─────────────
    // Data: event texts "Gain N Influence."
    private static final Map<String, Integer> EVENT_INFLUENCE = new HashMap<String, Integer>();
    static {
        EVENT_INFLUENCE.put("event_merchandising_b5",        Integer.valueOf(1));
        EVENT_INFLUENCE.put("de_event_merchandising_b5",     Integer.valueOf(1));
        EVENT_INFLUENCE.put("event_affirm_alliance",         Integer.valueOf(1));
        EVENT_INFLUENCE.put("event_destiny_fulfilled",       Integer.valueOf(2));
        EVENT_INFLUENCE.put("de_event_destiny_fulfilled",    Integer.valueOf(2));
        EVENT_INFLUENCE.put("event_secret_vorlon_aid",       Integer.valueOf(2));
        EVENT_INFLUENCE.put("de_event_secret_vorlon_aid",    Integer.valueOf(2));
    }

    // ── Events: card id → cards drawn by the playing player ─────────────────
    // Data: texts "Draw 2 cards." (+ Morden conditional, base 2 applied)
    private static final Map<String, Integer> EVENT_DRAW = new HashMap<String, Integer>();
    static {
        EVENT_DRAW.put("event_intrigues_mature",           Integer.valueOf(2));
        EVENT_DRAW.put("event_contact_with_vorlons",       Integer.valueOf(2));
        EVENT_DRAW.put("de_event_contact_with_vorlons",    Integer.valueOf(2));
        EVENT_DRAW.put("event_contact_with_shadows",       Integer.valueOf(2));
        EVENT_DRAW.put("de_event_contact_with_shadows",    Integer.valueOf(2));
    }

    // ── Events: card id → influence gained by EVERY player ──────────────────
    // Data: texts "All players gain 1 Influence."
    private static final Map<String, Integer> EVENT_ALL_INFLUENCE = new HashMap<String, Integer>();
    static {
        EVENT_ALL_INFLUENCE.put("event_balance",             Integer.valueOf(1));
        EVENT_ALL_INFLUENCE.put("de_event_balance",          Integer.valueOf(1));
        EVENT_ALL_INFLUENCE.put("event_for_the_common_good", Integer.valueOf(1));
        EVENT_ALL_INFLUENCE.put("de_event_for_the_common_good", Integer.valueOf(1));
    }

    // ── Conflicts: card id → influence the LOSER loses ──────────────────────
    // Data: texts "Loser loses N Influence."
    private static final Map<String, Integer> CONFLICT_LOSER_INFLUENCE = new HashMap<String, Integer>();
    static {
        CONFLICT_LOSER_INFLUENCE.put("conf_affirmation_of_peace",    Integer.valueOf(1));
        CONFLICT_LOSER_INFLUENCE.put("conf_condemn_deportations",    Integer.valueOf(1));
        CONFLICT_LOSER_INFLUENCE.put("de_conf_condemn_deportations", Integer.valueOf(1));
        CONFLICT_LOSER_INFLUENCE.put("conf_dishonor",                Integer.valueOf(1));
        CONFLICT_LOSER_INFLUENCE.put("conf_loss_of_support",         Integer.valueOf(1));
        CONFLICT_LOSER_INFLUENCE.put("de_conf_loss_of_support",      Integer.valueOf(1));
        CONFLICT_LOSER_INFLUENCE.put("conf_hate_crime",              Integer.valueOf(2));
        CONFLICT_LOSER_INFLUENCE.put("de_conf_hate_crime",           Integer.valueOf(2));
    }

    // ── Conflicts: card id → cards the LOSER discards at random ─────────────
    // Data: texts "Loser must discard N card(s)."
    private static final Map<String, Integer> CONFLICT_LOSER_DISCARD = new HashMap<String, Integer>();
    static {
        CONFLICT_LOSER_DISCARD.put("conf_bio_weapon_discovery",    Integer.valueOf(2));
        CONFLICT_LOSER_DISCARD.put("de_conf_bio_weapon_discovery", Integer.valueOf(2));
        CONFLICT_LOSER_DISCARD.put("conf_loss_of_support",         Integer.valueOf(1));
        CONFLICT_LOSER_DISCARD.put("de_conf_loss_of_support",      Integer.valueOf(1));
    }

    // ── Conflicts: card id → influence the WINNER steals from the loser ─────
    // Data: "Raid Shipping": "may steal 1 Influence from the loser."
    private static final Map<String, Integer> CONFLICT_WINNER_STEAL = new HashMap<String, Integer>();
    static {
        CONFLICT_WINNER_STEAL.put("conf_raid_shipping",    Integer.valueOf(1));
        CONFLICT_WINNER_STEAL.put("de_conf_raid_shipping", Integer.valueOf(1));
    }

    // ── Agendas: card id → influence at the start of each round ─────────────
    // Data: "Ongoing: Gain 1 Influence at the start of each round." /
    // "...if you have 2 or more characters in your Inner Circle, gain 1" (Deluxe).
    private static final Map<String, Integer> AGENDA_ROUND_INFLUENCE = new HashMap<String, Integer>();
    static {
        AGENDA_ROUND_INFLUENCE.put("agenda_higher_calling",       Integer.valueOf(1));
        AGENDA_ROUND_INFLUENCE.put("de_agenda_higher_calling",    Integer.valueOf(1));
        AGENDA_ROUND_INFLUENCE.put("de_agenda_servants_of_order", Integer.valueOf(1));
    }

    // ── Agendas: card id → extra influence when owner wins a Diplomacy conflict
    // Data: "Ongoing: Gain 1 additional Influence whenever you win a Diplomacy conflict."
    private static final Map<String, Integer> AGENDA_DIPLOMACY_WIN = new HashMap<String, Integer>();
    static {
        AGENDA_DIPLOMACY_WIN.put("agenda_a_rising_power",    Integer.valueOf(1));
        AGENDA_DIPLOMACY_WIN.put("de_agenda_a_rising_power", Integer.valueOf(1));
    }

    // ── Agendas: "Ongoing: Your fleets gain +1 Military." ───────────────────
    private static final Set<String> AGENDA_FLEET_PLUS1 = new HashSet<String>(
            Arrays.asList(new String[] {
                "agenda_total_war",       "de_agenda_total_war",
                "agenda_order_above_all", "de_agenda_order_above_all"
            }));

    // ── Enhancements: location income bonus, id → extra influence/round ─────
    // Data: "Exploitation": "That Location provides 1 extra Influence per round."
    // (No bonus JSON field exists for income; magnitudes are from the text.)
    private static final Map<String, Integer> ENH_LOCATION_INCOME = new HashMap<String, Integer>();
    static {
        ENH_LOCATION_INCOME.put("enh_exploitation",    Integer.valueOf(1));
        ENH_LOCATION_INCOME.put("de_enh_exploitation", Integer.valueOf(1));
    }

    // ── Public entry points ──────────────────────────────────────────────────

    /** Plays an Event card for p. Unknown ids keep the generic draw-1 floor. */
    public static void applyPlayEvent(GameState state, Player p, Card card) {
        String id = card.getId();
        boolean did = false;

        Integer inf = (Integer) EVENT_INFLUENCE.get(id);
        if (inf != null) {
            p.gainInfluence(inf.intValue());
            state.log(p.getName() + " gains " + inf + " influence (" + card.getTitle() + ").");
            did = true;
        }
        Integer draw = (Integer) EVENT_DRAW.get(id);
        if (draw != null) {
            p.drawCards(draw.intValue());
            state.log(p.getName() + " draws " + draw + " card(s) (" + card.getTitle() + ").");
            did = true;
        }
        Integer all = (Integer) EVENT_ALL_INFLUENCE.get(id);
        if (all != null) {
            for (Player q : state.getPlayers()) q.gainInfluence(all.intValue());
            state.log(card.getTitle() + ": all players gain " + all + " influence.");
            did = true;
        }
        if (!did) {
            p.drawCards(1);
            state.log(p.getName() + " plays " + card.getTitle()
                    + " (no specific effect registered; generic draw 1).");
        }
    }

    /** B5-0365: reveal uses the same id-keyed effect dispatch as an Event. */
    public static boolean revealContingency(GameState state, Player p,
                                            ContingencyCard card) {
        if (card == null || !card.reveal()) return false;
        state.log(p.getName() + " reveals contingency " + card.getTitle()
                + " (trigger: " + card.getTriggerCondition() + ").");
        applyPlayEvent(state, p, card);
        card.detach();
        if (p.getDeck() != null) p.getDeck().discard(card);
        return true;
    }

    /**
     * Plays an Enhancement card: attaches it to the strongest valid live
     * target and applies the JSON bonus fields. Faction/Global/Location/
     * Babylon 5 enhancements have no single-card target in the model; they
     * are held on the owner's enhancement list (location income bonuses are
     * applied by RulesEngine.startRound).
     *
     * B5-0473: fleet enhancements carrying the B5-0468 explicit opponent
     * target (Censure-class) attach to the CHOSEN opponent fleet instead of
     * the owner's best. The 0468 seam records the registry fact that
     * ATTACHED-scope bonuses are read from the TARGET owner's registry
     * (FleetCard.getEffectiveMilitary reads owner.effectiveStat), so the
     * penalty is granted INTO the opponent's Player registry, keyed to their
     * fleet id. An explicit target that cannot be resolved (unknown player,
     * unknown or face-down fleet) leaves the card held in play with no
     * registry effect — no self-fallback onto the owner's own fleet.
     */
    public static void applyPlayEnhancement(GameState state, Player p, EnhancementCard card) {
        String subtype = card.getSubtype() == null ? "" : card.getSubtype();
        String id = card.getId();

        if (subtype.endsWith("_FLEET")) {
            int delta = card.getMilitaryBonus();
            p.getEnhancements().add(card);

            // B5-0473: opponent-targeted path consumes the 0468 seam.
            if (card.hasExplicitTarget()) {
                Player victim = findPlayerByName(state, card.getTargetOwnerName());
                FleetCard target = victim == null
                        ? null : fleetById(victim, card.getTargetCardId());
                if (target != null && delta != 0) {
                    target.setOwner(victim);
                    victim.grantBonus(card.toAttachedBonus(StatKey.MILITARY,
                            Expiry.WHILE_IN_PLAY, state.getRoundNumber()));
                    state.log(p.getName() + " attaches " + card.getTitle()
                            + " to " + victim.getName() + "'s " + target.getTitle()
                            + " (Military " + (delta >= 0 ? "+" : "") + delta + ").");
                } else {
                    state.log(p.getName() + " holds " + card.getTitle()
                            + " (opponent target unavailable; held in play).");
                }
                return;
            }

            FleetCard target = bestFleet(p);
            if (target != null && delta != 0) {
                // B5-0366: route the bonus through the registry, not field mutation.
                target.setOwner(p);
                p.grantBonus(StatBonus.attached(card.getId(), StatKey.MILITARY, delta,
                        target.getId(), Expiry.WHILE_IN_PLAY, state.getRoundNumber()));
                state.log(p.getName() + " attaches " + card.getTitle() + " to "
                        + target.getTitle() + " (Military "
                        + (delta >= 0 ? "+" : "") + delta + ").");
            } else {
                state.log(p.getName() + " holds " + card.getTitle()
                        + " (no fleet target in play).");
            }
            return;
        }

        if (subtype.endsWith("_CHARACTER")) {
            CharacterCard target = bestCharacter(p);
            int dip = card.getDiplomacyBonus();
            int inr = card.getIntrigueBonus();
            int psi = card.getPsiBonus();
            int lead = card.getLeadershipBonus();
            p.getEnhancements().add(card);
            int r = state.getRoundNumber();
            if (target != null) {
                // B5-0366: route each non-zero bonus through the registry.
                target.setOwner(p);
                if (dip != 0)  p.grantBonus(StatBonus.attached(card.getId(), StatKey.DIPLOMACY,   dip,  target.getId(), Expiry.WHILE_IN_PLAY, r));
                if (inr != 0)  p.grantBonus(StatBonus.attached(card.getId(), StatKey.INTRIGUE,    inr,  target.getId(), Expiry.WHILE_IN_PLAY, r));
                if (psi != 0)  p.grantBonus(StatBonus.attached(card.getId(), StatKey.PSI,         psi,  target.getId(), Expiry.WHILE_IN_PLAY, r));
                if (lead != 0) p.grantBonus(StatBonus.attached(card.getId(), StatKey.LEADERSHIP, lead, target.getId(), Expiry.WHILE_IN_PLAY, r));
            }
            if (target != null && (dip != 0 || inr != 0 || psi != 0 || lead != 0)) {
                state.log(p.getName() + " attaches " + card.getTitle() + " to "
                        + target.getTitle() + " (+" + dip + " Dip, +" + inr + " Intr, +"
                        + psi + " Psi, +" + lead + " Lead).");
            } else {
                state.log(p.getName() + " holds " + card.getTitle()
                        + " (no character target in play).");
            }
            return;
        }

        if ("ENHANCEMENT_FACTION".equals(subtype)) {
            p.getEnhancements().add(card);
            int mil = card.getMilitaryBonus();
            // B5-066: faction-scope bonus goes through the registry.
            if (mil != 0) {
                p.grantBonus(StatBonus.faction(card.getId(), StatKey.MILITARY, mil,
                        p.getName(), Expiry.WHILE_IN_PLAY, state.getRoundNumber()));
                state.log(p.getName() + " plays " + card.getTitle()
                        + " (all own fleets " + (mil >= 0 ? "+" : "") + mil + " Military).");
            } else {
                state.log(p.getName() + " plays " + card.getTitle() + " (faction).");
            }
            return;
        }

        // GLOBAL / BABYLON5 / LOCATION / anything else: held on owner list;
        // ENH_LOCATION_INCOME ids are applied in RulesEngine.startRound.
        p.getEnhancements().add(card);
        state.log(p.getName() + " plays " + card.getTitle()
                + " (global/location effect; held in play).");
    }

    /**
     * Applies loser penalties / winner steal for a resolved conflict card.
     * Called after the winner's influence reward. winner != loser.
     */
    public static void applyConflictOutcome(GameState state, Conflict conflict,
                                            Player winner, Player loser) {
        // B5-0366/0376: war conflicts have card == null (no ConflictCard);
        // their card-specific loser penalties don't apply — the war influence
        // swing is handled at the resolution site in RulesEngine.
        if (conflict.getCard() == null) return;
        String id = conflict.getCard().getId();

        Integer loseInf = (Integer) CONFLICT_LOSER_INFLUENCE.get(id);
        if (loseInf != null) {
            loser.loseInfluence(loseInf.intValue());
            state.log(loser.getName() + " loses " + loseInf + " influence ("
                    + conflict.getCard().getTitle() + ").");
        }
        Integer discards = (Integer) CONFLICT_LOSER_DISCARD.get(id);
        if (discards != null) {
            int n = Math.min(discards.intValue(), loser.getHand().size());
            for (int i = 0; i < n; i++) {
                Card c = loser.getHand().remove(loser.getHand().size() - 1);
                loser.getDeck().discard(c);
            }
            if (n > 0) {
                state.log(loser.getName() + " discards " + n + " card(s) ("
                        + conflict.getCard().getTitle() + ").");
            }
        }
        Integer steal = (Integer) CONFLICT_WINNER_STEAL.get(id);
        if (steal != null) {
            int amount = Math.min(steal.intValue(), loser.getInfluence());
            if (amount > 0) {
                loser.loseInfluence(amount);
                winner.gainInfluence(amount);
                state.log(winner.getName() + " steals " + amount + " influence from "
                        + loser.getName() + " (" + conflict.getCard().getTitle() + ").");
            }
        }
    }

    /** Agenda effect when played: fleet-wide Military bonus agendas. */
    public static void applyAgendaOnPlay(GameState state, Player p, AgendaCard agenda) {
        if (AGENDA_FLEET_PLUS1.contains(agenda.getId())) {
            // B5-066: faction-scope bonus in the registry, not field mutation.
            p.grantBonus(StatBonus.faction(agenda.getId(), StatKey.MILITARY, 1,
                    p.getName(), Expiry.WHILE_IN_PLAY, state.getRoundNumber()));
            state.log(p.getName() + ": " + agenda.getTitle()
                    + " grants all own fleets +1 Military.");
        }
    }

    /** Agenda effect at the start of each round (call from startRound). */
    public static void applyAgendaStartOfRound(GameState state, Player p) {
        AgendaCard agenda = p.getAgenda();
        // B5-0364: hidden agendas have no effect on play until revealed (:520).
        if (agenda == null || agenda.isFaceDown()) return;
        Integer inf = (Integer) AGENDA_ROUND_INFLUENCE.get(agenda.getId());
        if (inf == null) return;
        // de_agenda_servants_of_order text: requires 2+ Inner Circle characters.
        if ("de_agenda_servants_of_order".equals(agenda.getId())
                && p.getInnerCircle().size() < 2) {
            return;
        }
        p.gainInfluence(inf.intValue());
        state.log(p.getName() + " gains " + inf + " influence from agenda "
                + agenda.getTitle() + ".");
    }

    /** Extra influence when the agenda owner wins a Diplomacy conflict. */
    public static int agendaDiplomacyWinBonus(Player p) {
        AgendaCard agenda = p.getAgenda();
        // B5-0364: hidden agendas have no effect on play until revealed (:520).
        if (agenda == null || agenda.isFaceDown()) return 0;
        Integer bonus = (Integer) AGENDA_DIPLOMACY_WIN.get(agenda.getId());
        return bonus == null ? 0 : bonus.intValue();
    }

    /** Location income bonus from held location enhancements (startRound). */
    public static int enhancementLocationIncomeBonus(Player p) {
        int bonus = 0;
        for (EnhancementCard e : p.getEnhancements()) {
            Integer inc = (Integer) ENH_LOCATION_INCOME.get(e.getId());
            if (inc != null) bonus += inc.intValue();
        }
        return bonus;
    }

    /** B5-0376 Phase B: true when the enhancement augments location income. */
    public static boolean isLocationIncomeEnhancement(EnhancementCard e) {
        return e != null && ENH_LOCATION_INCOME.containsKey(e.getId());
    }

    // ── Mercenaries (B5-0395) ─────────────────────────────────────────────────
    // A controlled mercenary's effect fires once at the MERCENARY phase for
    // its controller. The pool carries zero mercenary cards (B5-0386 no-
    // evidence verdict), so this table holds only the synthetic fixture used
    // by the conformance suite; real effects arrive when data work lands.
    // Unknown ids fire a LOUD no-op — never a silent pass, so a future card
    // added without an effect is caught in play.
    private static final Map<String, Integer> MERCENARY_FIXTURE_INFLUENCE =
            new HashMap<String, Integer>();
    static {
        MERCENARY_FIXTURE_INFLUENCE.put("mer_metric_fixture", Integer.valueOf(1));
    }

    /** Executes the controlled mercenary's effect for its controller. */
    public static void applyMercenaryAction(GameState state, Player controller, Card merc) {
        if (state == null || controller == null || merc == null) return;
        Integer inf = (Integer) MERCENARY_FIXTURE_INFLUENCE.get(merc.getId());
        if (inf != null) {
            controller.gainInfluence(inf.intValue());
            state.log(controller.getName() + " controls " + merc.getTitle()
                    + ", gaining " + inf + " influence.");
            return;
        }
        System.err.println("B5-0395: mercenary " + merc.getId() + " has no "
                + "registered effect — controller " + controller.getName()
                + " gets nothing (loud no-op; add an entry when data lands).");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** B5-0473: player lookup by name (model-level scan; GameState is out of
     *  this task's scope for a lookup method). Returns null when absent. */
    private static Player findPlayerByName(GameState state, String name) {
        if (name == null) return null;
        for (Player q : state.getPlayers()) {
            if (name.equals(q.getName())) return q;
        }
        return null;
    }

    /** B5-0473: face-up fleet lookup by card id on the target player.
     *  Face-down fleets are unresolvable (identity stays hidden). */
    private static FleetCard fleetById(Player victim, String cardId) {
        if (cardId == null) return null;
        for (FleetCard f : victim.getFleets()) {
            if (cardId.equals(f.getId()) && !f.isFaceDown()) return f;
        }
        return null;
    }

    private static FleetCard bestFleet(Player p) {
        FleetCard best = null;
        int bestMil = -1;
        for (FleetCard f : p.getFleets()) {
            // B5-066: use effective Military (includes bonuses) for target
            // selection. setOwner so the read path sees the registry.
            f.setOwner(p);
            int mil = f.getEffectiveMilitary();
            if (mil > bestMil) { bestMil = mil; best = f; }
        }
        return best;
    }

    private static CharacterCard bestCharacter(Player p) {
        CharacterCard best = p.getAmbassador();
        int bestStat = -1;
        if (best != null) {
            best.setOwner(p);
            bestStat = best.getPrimaryStatValue(ConflictType.DIPLOMACY)
                     + best.getPrimaryStatValue(ConflictType.INTRIGUE)
                     + best.getPrimaryStatValue(ConflictType.PSI)
                     + best.getPrimaryStatValue(ConflictType.MILITARY);
        }
        for (CharacterCard ch : p.getInnerCircle()) {
            ch.setOwner(p);
            int stat = ch.getPrimaryStatValue(ConflictType.DIPLOMACY)
                     + ch.getPrimaryStatValue(ConflictType.INTRIGUE)
                     + ch.getPrimaryStatValue(ConflictType.PSI)
                     + ch.getPrimaryStatValue(ConflictType.MILITARY);
            if (stat > bestStat) { bestStat = stat; best = ch; }
        }
        return best;
    }
}
