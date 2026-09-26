package b5ccg.ui;

import b5ccg.engine.RulesEngine;
import b5ccg.model.*;
import b5ccg.model.enums.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Java 6-compatible callback for card-selection events. */
interface CardSelectedListener {
    void onCardSelected(Card card);
}

public class HandPanel extends JPanel {

    private static final int CARD_W   = 110;
    private static final int CARD_H   = 148;
    private static final int OVERLAP  = 18;

    private List<Card>      hand = new ArrayList<Card>();
    private int             selectedIndex = -1;
    private CardSelectedListener onCardSelected;

    // B5-0348: filtering + sorting + playable highlight
    private RulesEngine     rules;
    private Player          humanPlayer;
    // B5-0361: resolved-conflict context for the eligible-aftermath highlight.
    // Null conflict/state are handled defensively (never NPE):
    // no conflict held means aftermaths are simply not eligible.
    private Conflict        resolvedConflict;
    private GameState       resolvedState;
    private boolean[]       typeFilter = new boolean[CardType.values().length];
    private boolean[]       factionFilter = new boolean[Faction.values().length];
    private int             sortMode = 0;   // 0=unsorted, 1=cost asc, 2=cost desc
    private boolean         showUnplayable = true;

    // fill filters with defaults: all types on, all factions on
    {
        for (int i = 0; i < typeFilter.length; i++) typeFilter[i] = true;
        for (int i = 0; i < factionFilter.length; i++) factionFilter[i] = true;
    }

    public HandPanel() {
        setBackground(new Color(10, 25, 10));
        setPreferredSize(new Dimension(1280, 170));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    // B5-0348: inject rules engine + human faction for affordability/playability checks
    public void setRules(RulesEngine rules, Faction humanFaction) {
        this.rules = rules;
        // humanFaction stored for filter purposes; humanPlayer for affordability
    }

    public void setHumanPlayer(Player human) { this.humanPlayer = human; }

    // B5-0348: type filter control
    public void setTypeFilter(CardType type, boolean show) {
        typeFilter[type.ordinal()] = show;
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { repaint(); }
        });
    }

    // B5-0348: faction filter control
    public void setFactionFilter(Faction faction, boolean show) {
        factionFilter[faction.ordinal()] = show;
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { repaint(); }
        });
    }

    // B5-0348: sort mode — 0=insertion order, 1=cost ascending, 2=cost descending
    public void setSortMode(int mode) {
        sortMode = mode;
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { repaint(); }
        });
    }

    // B5-0348: toggle dimming of unaffordable cards
    public void setShowUnplayable(boolean show) {
        showUnplayable = show;
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { repaint(); }
        });
    }

    public void setOnCardSelected(CardSelectedListener cb) { onCardSelected = cb; }

    // B5-0348: update hand with affordability checks
    public void update(List<Card> hand, RulesEngine rules, Player human) {
        this.hand = new ArrayList<Card>(hand);
        this.rules = rules;
        this.humanPlayer = human;
        selectedIndex = -1;
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { repaint(); }
        });
    }

    /** B5-0361: MainWindow brokers the UI-held conflict + state here each
     *  refresh; the highlight is an eligibility READOUT against the last
     *  resolved conflict (the controller's aftermath auto-play is AI-only,
     *  so this never promises a human play action). */
    public void setResolvedConflictContext(Conflict c, GameState s) {
        this.resolvedConflict = c;
        this.resolvedState = s;
    }

    // backward-compatible overload for callers that don't pass rules/player
    public void update(List<Card> hand) {
        this.hand = new ArrayList<Card>(hand);
        selectedIndex = -1;
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { repaint(); }
        });
    }

    private void handleClick(int mx, int my) {
        // B5-0348: account for filtered view — only iterate visible cards
        int x = 10;
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (!cardVisible(card)) { x += (CARD_W - OVERLAP); continue; }
            int y = (getHeight() - CARD_H) / 2;
            int w = CARD_W;
            if (mx >= x && mx <= x + w && my >= y && my <= y + CARD_H) {
                selectedIndex = hand.indexOf(card);
                if (onCardSelected != null) onCardSelected.onCardSelected(card);
                repaint();
                return;
            }
            x += (CARD_W - OVERLAP);
        }
        selectedIndex = -1;
        repaint();
    }

    // B5-0348: returns true when card passes all active filters
    private boolean cardVisible(Card card) {
        if (!typeFilter[card.getType().ordinal()]) return false;
        if (!factionFilter[card.getFaction().ordinal()]) return false;
        return true;
    }

    // B5-0361: eligible-aftermath readout — true when THIS aftermath has at
    // least one legal target on the UI-held resolved conflict (6-arg
    // canPlayAftermath per candidate: non-Participant aftermaths target the
    // initiator only, Participant aftermaths any participant; D4 registry
    // consulted via the state). Never a play promise.
    private boolean aftermathEligible(AftermathCard am) {
        if (rules == null || humanPlayer == null) return false;
        Conflict c = resolvedConflict;
        if (c == null || !c.isResolved() || c.getWinner() == null) return false;
        boolean initiatorWon = (c.getWinner() == c.getInitiator());
        for (Player cand : resolvedState.getPlayers()) {
            if (rules.canPlayAftermath(humanPlayer, am, c, initiatorWon, cand,
                                       resolvedState)) {
                return true;
            }
        }
        return false;
    }

    // B5-0348: returns true when the human player can afford/play this card via a spending action
    private boolean cardPlayable(Card card) {
        if (rules == null || humanPlayer == null) return true;
        // B5-0361: aftermaths are conflict-resolved resources — the engine
        // never offers humans a voluntary play; "playable" = an eligible
        // target exists on the UI-held resolved conflict (replaces the old
        // misleading generic-tail answer of always-true).
        if (card instanceof AftermathCard) return aftermathEligible((AftermathCard) card);
        if (card instanceof CharacterCard) {
            CharacterCard ch = (CharacterCard) card;
            // Sponsor (recruit from hand) affordability
            if (humanPlayer.getHand().contains(ch)
                    && !ch.isRotated() && !ch.isFaceDown()) {
                if (rules.canRecruit(humanPlayer, ch)) return true;
            }
            // Promote affordability (card is in supporting role)
            if (humanPlayer.getSupportingRole().contains(ch)
                    && !ch.isRotated() && !ch.isFaceDown()) {
                if (rules.canPromote(humanPlayer, ch)) return true;
            }
            // Build Influence is not card-specific; handled separately
            return false;
        }
        // Non-character cards: conflict cards are playable if faction-legal (checked elsewhere)
        // Agenda/Event/Enhancement/Group/Location cards: playable via playCard if faction-legal
        if (card.getFaction().isPlayableBy(humanPlayer.getFaction())) {
            return humanPlayer.getHand().contains(card);
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (hand.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // B5-0348: build a sorted+filtered view
        List<Card> visible = new ArrayList<Card>();
        for (Card c : hand) {
            if (cardVisible(c)) visible.add(c);
        }
        if (sortMode == 1) {
            Collections.sort(visible, new Comparator<Card>() {
                @Override public int compare(Card a, Card b) {
                    return Integer.compare(a.getCost(), b.getCost());
                }
            });
        } else if (sortMode == 2) {
            Collections.sort(visible, new Comparator<Card>() {
                @Override public int compare(Card a, Card b) {
                    return Integer.compare(b.getCost(), a.getCost());
                }
            });
        }

        int startX = 10;
        int baseY  = (getHeight() - CARD_H) / 2;

        for (int i = 0; i < visible.size(); i++) {
            Card card = visible.get(i);
            int x = startX + i * (CARD_W - OVERLAP);
            int y = baseY - (card == getSelectedCard() ? 14 : 0);
            boolean selected = (card == getSelectedCard());
            boolean playable = cardPlayable(card);
            boolean dimmed = !showUnplayable && !playable;
            drawCard(g2, card, x, y, selected, dimmed);
        }
    }

    // B5-0348 helper: is this card the selected one?
    private Card getSelectedCard() {
        if (selectedIndex < 0 || selectedIndex >= hand.size()) return null;
        return hand.get(selectedIndex);
    }

    private void drawCard(Graphics2D g, Card card, int x, int y, boolean selected,
                          boolean dimmed) {
        Color savedColor = g.getColor();

        // Shadow
        if (selected) {
            g.setColor(new Color(0, 200, 0, 80));
            g.fillRoundRect(x - 3, y - 3, CARD_W + 6, CARD_H + 6, 10, 10);
        }

        // Background
        g.setColor(typeColor(card));
        g.fillRoundRect(x, y, CARD_W, 16, 4, 4);
        g.setColor(new Color(20, 20, 50));
        g.fillRoundRect(x, y + 14, CARD_W, CARD_H - 14, 4, 4);

        // B5-0348: dim unaffordable/unplayable cards
        if (dimmed) {
            g.setColor(new Color(0, 0, 0, 128));
            g.fillRoundRect(x, y, CARD_W, CARD_H, 8, 8);
        }

        // B5-0361: eligible-aftermath highlight — green tag when this
        // aftermath has a legal target on the last resolved conflict (an
        // eligibility READOUT; the engine never offers humans a voluntary
        // aftermath play). Drawn after the dim overlay so it is never hidden.
        if (card instanceof AftermathCard && aftermathEligible((AftermathCard) card)) {
            g.setColor(new Color(30, 140, 30));
            g.fillRoundRect(x + 4, y + CARD_H - 16, CARD_W - 8, 13, 6, 6);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            g.drawString("ELIGIBLE", x + 16, y + CARD_H - 6);
        }

        // Border
        g.setColor(selected ? new Color(100, 220, 100) : new Color(140, 110, 40));
        g.setStroke(new BasicStroke(selected ? 2 : 1));
        g.drawRoundRect(x, y, CARD_W, CARD_H, 8, 8);

        // Type bar label
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 8));
        g.drawString(card.getType().toString(), x + 3, y + 11);

        // Title
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        g.setColor(Color.WHITE);
        String title = card.getTitle().length() > 16 ? card.getTitle().substring(0, 15) + "…" : card.getTitle();
        g.drawString(title, x + 3, y + 26);

        // Faction
        g.setFont(new Font("SansSerif", Font.ITALIC, 8));
        g.setColor(new Color(180, 180, 180));
        g.drawString(card.getFaction().toString(), x + 3, y + 37);

        // B5-0333 F13: influence cost on card face
        int cost = card.getCost();
        if (cost > 0) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 8));
            g.setColor(new Color(220, 200, 100));
            g.drawString("Cost: " + cost + " INF", x + 3, y + 46);
        }

        // B5-0348: playable indicator — green dot for affordable cards
        if (!dimmed && cost > 0 && card instanceof CharacterCard) {
            g.setColor(new Color(100, 220, 100));
            g.fillOval(x + CARD_W - 14, y + 8, 6, 6);
        }

        // Stats
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        g.setColor(dimmed ? new Color(100, 100, 100) : new Color(160, 220, 160));
        if (card instanceof CharacterCard) {
            CharacterCard ch = (CharacterCard) card;
            g.drawString("D" + ch.getDiplomacy() + " I" + ch.getIntrigue()
                + " P" + ch.getPsi() + " L" + ch.getLeadership(), x + 3, y + 50);
        } else if (card instanceof FleetCard) {
            g.setColor(dimmed ? new Color(100, 100, 100) : new Color(160, 190, 220));
            g.drawString("Military: " + ((FleetCard) card).getMilitary(), x + 3, y + 50);
        } else if (card instanceof ConflictCard) {
            ConflictCard cc = (ConflictCard) card;
            g.setColor(dimmed ? new Color(100, 100, 100) : new Color(220, 140, 140));
            g.drawString(cc.getConflictType() + "  +" + cc.getInfluenceReward() + " INF", x + 3, y + 50);
        }

        // B5-0519: damage-state readout, mirroring the B5-0516 board marker
        // via the same B5-0368 Card base-class API (getDamageTokens,
        // getSevereDamageTokens, isNeutralized). Right-aligned on the stats
        // baseline so it cannot collide with the left-aligned stat string;
        // drawn after the dim overlay so the state stays readable (the same
        // never-hidden principle as the B5-0361 ELIGIBLE tag). Dormant in
        // normal play — damage targets in-play cards and hand cards are
        // fresh — but the state shows if the engine ever applies it.
        if (!card.isFaceDown()) {
            int dmg = card.getDamageTokens();
            int sev = card.getSevereDamageTokens();
            boolean neutral = card.isNeutralized();
            String state;
            if (dmg > 0 || sev > 0) {
                state = (neutral ? "NEUT " : "DMG:")
                    + dmg + (sev > 0 ? "+" + sev : "");
            } else {
                state = neutral ? "NEUT" : "";
            }
            if (state.length() > 0) {
                g.setColor(new Color(255, 80, 80));
                g.setFont(new Font("SansSerif", Font.BOLD, 7));
                g.drawString(state,
                    x + CARD_W - g.getFontMetrics().stringWidth(state) - 3, y + 50);
            }
        }

        // Card text (abbreviated)
        g.setFont(new Font("SansSerif", Font.PLAIN, 7));
        g.setColor(dimmed ? new Color(100, 100, 100) : new Color(180, 180, 180));
        String text = card.getText() != null ? card.getText() : "";
        drawWrappedText(g, text, x + 3, y + 62, CARD_W - 6, CARD_H - 68);

        // Rarity dot
        g.setColor(rarityColor(card));
        g.fillOval(x + CARD_W - 12, y + CARD_H - 12, 8, 8);

        // Index
        g.setColor(new Color(100, 100, 100));
        g.setFont(new Font("SansSerif", Font.PLAIN, 7));
        g.drawString(String.valueOf(hand.indexOf(card) + 1 > 0 ? hand.indexOf(card) + 1 : ""), x + 3, y + CARD_H - 3);

        g.setColor(savedColor);
    }

    private void drawWrappedText(Graphics2D g, String text, int x, int y, int maxW, int maxH) {
        FontMetrics fm = g.getFontMetrics();
        int lineH = fm.getHeight();
        int ty = y + lineH;
        StringBuilder line = new StringBuilder();
        for (String word : text.split("\\s+")) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) > maxW) {
                if (ty + lineH > y + maxH) { g.drawString(line + "…", x, ty); return; }
                g.drawString(line.toString(), x, ty);
                ty += lineH;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0 && ty <= y + maxH) g.drawString(line.toString(), x, ty);
    }

    private Color typeColor(Card c) {
        switch (c.getType()) {
            case CHARACTER:   return new Color(50, 90, 150);
            case FLEET:       return new Color(60, 70, 140);
            case CONFLICT:    return new Color(150, 50, 50);
            case AGENDA:      return new Color(150, 120, 30);
            case AFTERMATH:   return new Color(90, 50, 120);
            case EVENT:       return new Color(30, 110, 70);
            case ENHANCEMENT: return new Color(70, 130, 70);
            case GROUP:       return new Color(110, 90, 50);
            case LOCATION:    return new Color(50, 110, 110);
            default:          return new Color(50, 50, 50);
        }
    }

    private Color rarityColor(Card c) {
        switch (c.getRarity()) {
            case RARE:      return new Color(255, 180, 0);
            case UNCOMMON:  return new Color(180, 180, 180);
            case FIXED:     return new Color(100, 180, 255);
            case PROMO:     return new Color(255, 120, 200);
            default:        return new Color(120, 120, 120);
        }
    }
}
