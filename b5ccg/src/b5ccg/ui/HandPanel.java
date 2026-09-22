package b5ccg.ui;

import b5ccg.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

    public HandPanel() {
        setBackground(new Color(10, 25, 10));
        setPreferredSize(new Dimension(1280, 170));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    public void setOnCardSelected(CardSelectedListener cb) { onCardSelected = cb; }

    public void update(List<Card> hand) {
        this.hand = new ArrayList<Card>(hand);
        selectedIndex = -1;
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { repaint(); }
        });
    }

    private void handleClick(int mx, int my) {
        for (int i = hand.size() - 1; i >= 0; i--) {
            int x = 10 + i * (CARD_W - OVERLAP);
            int y = (getHeight() - CARD_H) / 2;
            if (mx >= x && mx <= x + CARD_W && my >= y && my <= y + CARD_H) {
                selectedIndex = i;
                if (onCardSelected != null) onCardSelected.onCardSelected(hand.get(i));
                repaint();
                return;
            }
        }
        selectedIndex = -1;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (hand.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int startX = 10;
        int baseY  = (getHeight() - CARD_H) / 2;

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            int  x    = startX + i * (CARD_W - OVERLAP);
            int  y    = baseY - (i == selectedIndex ? 14 : 0);
            drawCard(g2, card, x, y, i == selectedIndex);
        }
    }

    private void drawCard(Graphics2D g, Card card, int x, int y, boolean selected) {
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

        // Stats
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        g.setColor(new Color(160, 220, 160));
        if (card instanceof CharacterCard) {
            CharacterCard ch = (CharacterCard) card;
            g.drawString("D" + ch.getDiplomacy() + " I" + ch.getIntrigue()
                + " P" + ch.getPsi() + " L" + ch.getLeadership(), x + 3, y + 50);
        } else if (card instanceof FleetCard) {
            g.setColor(new Color(160, 190, 220));
            g.drawString("Military: " + ((FleetCard) card).getMilitary(), x + 3, y + 50);
        } else if (card instanceof ConflictCard) {
            ConflictCard cc = (ConflictCard) card;
            g.setColor(new Color(220, 140, 140));
            g.drawString(cc.getConflictType() + "  +" + cc.getInfluenceReward() + " INF", x + 3, y + 50);
        }

        // Card text (abbreviated)
        g.setFont(new Font("SansSerif", Font.PLAIN, 7));
        g.setColor(new Color(180, 180, 180));
        String text = card.getText() != null ? card.getText() : "";
        drawWrappedText(g, text, x + 3, y + 62, CARD_W - 6, CARD_H - 68);

        // Rarity dot
        g.setColor(rarityColor(card));
        g.fillOval(x + CARD_W - 12, y + CARD_H - 12, 8, 8);

        // Index
        g.setColor(new Color(100, 100, 100));
        g.setFont(new Font("SansSerif", Font.PLAIN, 7));
        g.drawString(String.valueOf(hand.indexOf(card) + 1 > 0 ? hand.indexOf(card) + 1 : ""), x + 3, y + CARD_H - 3);
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
