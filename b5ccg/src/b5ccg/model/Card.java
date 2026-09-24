package b5ccg.model;

import b5ccg.model.enums.*;
import java.util.*;

public abstract class Card {
    private final String   id;
    private final String   title;
    private final CardType type;
    private final String   subtype;
    private final Rarity   rarity;
    private final Faction  faction;
    private final CardSet  cardSet;
    private final String   imageKey;
    private final String   text;

    private boolean faceDown = false;
    private boolean rotated  = false;
    private int damageTokens = 0;
    private int severeDamageTokens = 0;
    private boolean neutralized = false;
    private boolean neutralizedThisTurn = false;
    private int strifeMarks = 0;

    // B5-0323: influence cost to bring this card into play (rulebook
    // §Anatomy item 2). Defaults to 0 — no card currently carries the
    // key (B5-0311 C1); backfilling prices is a later data task.
    private int cost = 0;
    private final List<ContingencyCard> contingencies = new ArrayList<ContingencyCard>();

    protected Card(String id, String title, CardType type, String subtype,
                   Rarity rarity, Faction faction, CardSet cardSet,
                   String imageKey, String text) {
        this.id       = id;
        this.title    = title;
        this.type     = type;
        this.subtype  = subtype;
        this.rarity   = rarity;
        this.faction  = faction;
        this.cardSet  = cardSet;
        this.imageKey = imageKey;
        this.text     = text;
    }

    public String   getId()       { return id; }
    public String   getTitle()    { return title; }
    public CardType getType()     { return type; }
    public String   getSubtype()  { return subtype; }
    public Rarity   getRarity()   { return rarity; }
    public Faction  getFaction()  { return faction; }
    public CardSet  getCardSet()  { return cardSet; }
    public String   getImageKey() { return imageKey; }
    public String   getText()     { return text; }

    public boolean isFaceDown() { return faceDown; }
    public boolean isRotated()  { return rotated; }

    public void setFaceDown(boolean v) { faceDown = v; }
    public void setRotated(boolean v)  { rotated  = v; }
    public int getStrifeMarks() { return strifeMarks; }
    public void setStrifeMarks(int count) { strifeMarks = Math.max(0, count); }

    /** B5-0323: cost accessors. Negative values clamp to 0. */
    public int  getCost()      { return cost; }
    public void setCost(int c) { cost = Math.max(0, c); }

    public void rotate()   { rotated  = true; }
    public void unrotate() { rotated  = false; }
    /** Legacy B5-0309 conflict damage remains a face-down flip, not a token hit. */
    public void damage()   { faceDown = true; neutralized = true; neutralizedThisTurn = true; onNeutralized(); }
    public void heal()     { faceDown = false; neutralized = false; damageTokens = 0; severeDamageTokens = 0; }

    public int getDamageTokens() { return damageTokens; }
    public int getSevereDamageTokens() { return severeDamageTokens; }
    public boolean isNeutralized() { return neutralized; }
    public boolean wasNeutralizedThisTurn() { return neutralizedThisTurn; }
    public boolean canActAfterNeutralization() { return !neutralizedThisTurn; }
    public void resetNeutralizedTurnLock() { neutralizedThisTurn = false; }

    /** Highest ability before damage reductions; subclasses supply their live bonus-aware value. */
    public int getGreatestAbility() { return 0; }

    /** Adds normal damage, converting the neutralizing amount to severe overflow. */
    public int applyDamage(int amount) {
        if (amount <= 0 || neutralized) return 0;
        damageTokens += amount;
        return reconcileDamage();
    }

    /** Current damage dealt by an attack using this card's conflict ability. */
    public int getAttackDamage(ConflictType type) {
        return Math.max(0, getPrimaryStatValue(type)) + 2 * strifeMarks;
    }

    /** Apply additional severe damage to a card already neutralized. */
    public int applySevereDamage(int amount) {
        if (amount <= 0 || !neutralized) return 0;
        severeDamageTokens += amount;
        return amount;
    }

    /** Rechecks neutralization after a bonus changes; returns newly severe overflow. */
    public int reconcileDamage() {
        int greatest = Math.max(1, getGreatestAbility());
        if (neutralized || damageTokens < greatest) return 0;
        int overflow = damageTokens - greatest;
        damageTokens = 0;
        severeDamageTokens += overflow;
        neutralized = true;
        neutralizedThisTurn = true;
        faceDown = true;
        onNeutralized();
        return overflow;
    }

    /** Notification for dependent cards (e.g. a character leading a fleet). */
    protected void onNeutralized() { }

    /** Fleet leadership neutralizes a character without transferring damage. */
    public void neutralizeFromFleet() {
        if (neutralized) return;
        neutralized = true;
        neutralizedThisTurn = true;
        faceDown = true;
        onNeutralized();
    }

    /** Heal normal damage. A neutralized card only flips up once severe damage is gone. */
    public int healDamage() {
        if (neutralized) {
            if (severeDamageTokens > 0) severeDamageTokens--;
            else {
                neutralized = false;
                faceDown = false;
                damageTokens = 0;
            }
            return 1;
        }
        int healed = damageTokens;
        damageTokens = 0;
        return healed;
    }

    public int repairDamage(int amount) {
        if (amount <= 0 || neutralized) return 0;
        int repaired = Math.min(amount, damageTokens);
        damageTokens -= repaired;
        return repaired;
    }

    /** B5-0365: face-down contingency cards currently placed under this host. */
    public int getContingencyCount() { return contingencies.size(); }
    boolean addContingency(ContingencyCard contingency) {
        if (contingency == null || contingencies.contains(contingency)) return false;
        contingencies.add(contingency);
        return true;
    }
    boolean removeContingency(ContingencyCard contingency) {
        return contingencies.remove(contingency);
    }

    /** Returns the stat value used in the given conflict type for this card. */
    public abstract int getPrimaryStatValue(ConflictType type);

    @Override
    public String toString() { return title + " [" + type + "/" + rarity + "]"; }
}
