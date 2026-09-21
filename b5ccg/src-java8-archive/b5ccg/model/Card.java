package b5ccg.model;

import b5ccg.model.enums.*;

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

    public void rotate()   { rotated  = true; }
    public void unrotate() { rotated  = false; }
    public void damage()   { faceDown = true; }
    public void heal()     { faceDown = false; }

    /** Returns the stat value used in the given conflict type for this card. */
    public abstract int getPrimaryStatValue(ConflictType type);

    @Override
    public String toString() { return title + " [" + type + "/" + rarity + "]"; }
}
