package b5ccg.model;

import b5ccg.model.enums.*;

public class FleetCard extends Card {
    private final int military;

    // B5-0336 (proposal §3.6): fleet class for participation "fleetSubtypes"
    // filters (PICKET, COLONIAL, UTILITY, ...). Null/absent = class unknown;
    // such a fleet cannot be proven eligible for a fleetSubtypes-filtered
    // conflict and is excluded from it (proposal §3.6), but is otherwise
    // unaffected. Population of the data field is a separate data task.
    private String fleetClass;

    // B5-0337 (audit D5): the character currently leading this fleet —
    // rulebook (Action Details, Support or Oppose): "one character per fleet
    // may rotate to add his Leadership Ability to the Military Ability of any
    // fleet". Null = unled. Set through RulesEngine.executeLeadFleet (which
    // rotates the leader); cleared by RulesEngine.startRound — the relation
    // is a per-round rotation, not a permanent attachment.
    private CharacterCard leader;

    public FleetCard(String id, String title, String subtype,
                     Rarity rarity, Faction faction, CardSet cardSet,
                     String imageKey, String text, int military) {
        super(id, title, CardType.FLEET, subtype, rarity, faction,
              cardSet, imageKey, text);
        this.military = military;
    }

    public int getMilitary() { return isRotated() ? 0 : military; }

    /** B5-0366: the owning player, so this card can read its registry bonuses. */
    private Player owner;
    public void setOwner(Player p) { owner = p; }

    /**
     * B5-0337: the fleet's Military with fleet leadership applied. A rotated,
     * face-up leader adds his Leadership (his rotation IS the rulebook
     * requirement for leading); a damaged leader adds 0. The fleet itself
     * stays unrotated while fighting — a rotated fleet contributes 0.
     * B5-0366: PLUS any ATTACHED/FACTION bonuses from the owner registry.
     * Clamped at 0 (the D10 floor-at-1 dies — see proposal §3.2).
     */
    public int getEffectiveMilitary() {
        reconcileDamage();
        if (isFaceDown()) return 0;
        // Rotation pays for participation/actions; it does not erase ability
        // from a card already committed to a conflict.
        int base = military;
        if (leader != null && leader.isRotated() && !leader.isFaceDown()) {
            if (owner != null) leader.setOwner(owner);
            base += leader.getEffectiveStat(StatKey.LEADERSHIP);
        }
        if (owner != null) {
            base = owner.effectiveStat(getId(), StatKey.MILITARY, base, true);
        }
        return Math.max(0, base - getDamageTokens());
    }

    @Override
    public int getGreatestAbility() {
        int base = military;
        if (leader != null && leader.isRotated() && !leader.isFaceDown()) {
            if (owner != null) leader.setOwner(owner);
            base += leader.getEffectiveStat(StatKey.LEADERSHIP);
        }
        if (owner != null) base = owner.effectiveStat(getId(), StatKey.MILITARY, base, true);
        return Math.max(0, base);
    }

    @Override
    protected void onNeutralized() {
        if (leader != null) leader.neutralizeFromFleet();
    }

    /** B5-0336: fleet class accessors (null = unknown, see field comment). */
    public String getFleetClass()            { return fleetClass; }
    public void   setFleetClass(String fc)   { fleetClass = (fc == null || fc.trim().length() == 0) ? null : fc.trim(); }

    @Override
    public int getPrimaryStatValue(ConflictType type) {
        if (type != ConflictType.MILITARY) return 0;
        return getEffectiveMilitary();
    }

    /** B5-0337: fleet-leader relation (null = unled; see field comment). */
    public CharacterCard getLeader()           { return leader; }
    public void          setLeader(CharacterCard ch) { leader = ch; }
}
