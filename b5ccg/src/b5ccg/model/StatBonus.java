package b5ccg.model;

import b5ccg.model.enums.*;

/** B5-0366 (B5-0357 proposal §3.1): an immutable description of a single
 *  stat bonus sourced by a card. Replaces the permanent mutation in
 *  CharacterCard.applyStatDelta / FleetCard.applyMilitaryDelta.
 *
 *  Java 6 — final fields, no lambdas/streams/diamonds beyond 6.
 */
public final class StatBonus {
    /** The card that granted this bonus ("" for engine-granted, e.g. leader). */
    public final String       sourceCardId;
    /** The stat this bonus modifies. */
    public final StatKey      stat;
    /** The modifier (may be negative for penalties). */
    public final int          delta;
    /** Where the bonus applies (ATTACHED to a card, or FACTION-wide). */
    public final BonusScope   scope;
    /** The cardId the ATTACHED scope points at (null for FACTION). */
    public final String       targetCardId;
    /** The owning player's name the FACTION scope points at (null for ATTACHED). */
    public final String       ownerName;
    /** Lifetime until swept or removed. */
    public final Expiry       expiry;
    /** True only for "specifically increases Psi" cards (Psi-from-zero rule). */
    public final boolean      psiFromZero;
    /** False = same-source replacement semantics (non-cumulative). */
    public final boolean      cumulative;
    /** GameState.getRoundNumber() when granted — for expiry sweeps. */
    public final int          createdRound;

    public StatBonus(String sourceCardId, StatKey stat, int delta,
                     BonusScope scope, String targetCardId, String ownerName,
                     Expiry expiry, boolean psiFromZero, boolean cumulative,
                     int createdRound) {
        this.sourceCardId = sourceCardId == null ? "" : sourceCardId;
        this.stat        = stat;
        this.delta       = delta;
        this.scope       = scope;
        this.targetCardId = targetCardId;
        this.ownerName   = ownerName;
        this.expiry      = expiry;
        this.psiFromZero = psiFromZero;
        this.cumulative  = cumulative;
        this.createdRound = createdRound;
    }

    /** Convenience factory for an ATTACHED non-Psi bonus. */
    public static StatBonus attached(String sourceId, StatKey stat, int delta,
                                     String targetCardId, Expiry expiry,
                                     int createdRound) {
        return new StatBonus(sourceId, stat, delta, BonusScope.ATTACHED,
                             targetCardId, null, expiry, false, true, createdRound);
    }

    /** Convenience factory for a FACTION-scope bonus (target-card-independent). */
    public static StatBonus faction(String sourceId, StatKey stat, int delta,
                                    String ownerName, Expiry expiry,
                                    int createdRound) {
        return new StatBonus(sourceId, stat, delta, BonusScope.FACTION,
                             null, ownerName, expiry, false, true, createdRound);
    }
}
