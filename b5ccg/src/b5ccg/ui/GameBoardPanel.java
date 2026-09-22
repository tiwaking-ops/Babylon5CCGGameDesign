package b5ccg.ui;

import b5ccg.model.*;
import b5ccg.model.enums.ConflictType;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GameBoardPanel extends JPanel {

    private GameState state;

    public GameBoardPanel() {
        setBackground(new Color(15, 35, 15));
        setPreferredSize(new Dimension(1280, 560));
    }

    public void update(GameState s) {
        this.state = s;
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (state == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<Player> players = state.getPlayers();
        int cols   = players.size();
        int zoneW  = getWidth() / cols;

        for (int i = 0; i < players.size(); i++) {
            drawZone(g2, players.get(i), i * zoneW, 0, zoneW, getHeight());
        }

        // Active conflict banner
        if (state.getActiveConflict() != null) {
            Conflict c = state.getActiveConflict();
            g2.setColor(new Color(200, 50, 50, 200));
            g2.fillRoundRect(getWidth() / 4, getHeight() / 2 - 20, getWidth() / 2, 40, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            // B5-0329 F10: assistant status overlay
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g.setColor(new Color(150, 170, 200));
            int pageW = getWidth();
            int pageH = getHeight();
            g.drawString("B5-0329 ASSISTANTS (F10):", pageW - 112, 14);
            g.setFont(new Font("Monospaced", Font.PLAIN, 8));
            int ay = 26;
            String[] assistants = {
                "Ambassador — advisor on diplomacy/intrigue (B5-0325 F9)",
                "Lokai-Commander — conflict type threats (B5-0325 F11)",
                "Refer-Consultant — command set summary (B5-0325 F12)",
                "Strategy-Analyst — hand valuation (B5-0325 F10)",
                "Sanction-Interpreter — card ability guide (B5-0325 F13)"
            };
            for (String txt : assistants) {
                if (ay > pageH - 20) break;
                g.drawString(txt, pageW - 112, ay);
                ay += 10;
            }
            String msg = "CONFLICT: " + c.getCard().getTitle()
                + "  [" + c.getConflictType() + "]";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2 + 6);

            // Sides readout (B5-0309 / D14): committed-supporters vs committed-opposers,
            // with totals. Blank line added under the banner for separation.
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("sides committed (B5-0309):", (getWidth() - fm.stringWidth("sides committed (B5-0309):")) / 2, (getHeight() / 2) + 30);
            String sideLine = c.getSupporters().size() + " support (" + c.supportTotal() + ")   |   " + c.getOpposers().size() + " oppose (" + c.oppositionTotal() + ")";
            g.drawString(sideLine, (getWidth() - fm.stringWidth(sideLine)) / 2, (getHeight() / 2) + 46);
            }
    }

    private void drawZone(Graphics2D g, Player p, int x, int y, int w, int h) {
        boolean active = state.getActivePlayer() == p;
        g.setColor(active ? new Color(35, 70, 35) : new Color(20, 45, 20));
        g.fillRect(x + 2, y + 2, w - 4, h - 4);

        g.setColor(active ? new Color(100, 220, 100) : new Color(50, 100, 50));
        g.setStroke(new BasicStroke(active ? 2.5f : 1));
        g.drawRect(x + 2, y + 2, w - 4, h - 4);

        // Header
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(factionColor(p));
        g.drawString(p.getName(), x + 8, y + 18);

        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(200, 200, 120));
        g.drawString("Influence: " + p.getInfluence(), x + 8, y + 33);
        g.drawString(p.getFaction().toString(), x + 8, y + 47);

        // Phase indicator
        if (state.getActivePlayer() == p) {
            g.setColor(new Color(255, 220, 60));
            g.setFont(new Font("SansSerif", Font.ITALIC, 10));
            g.drawString("[" + state.getPhase() + "]", x + w - 90, y + 18);
        }

        // Ambassador
        if (p.getAmbassador() != null) {
            drawMiniCard(g, p.getAmbassador(), x + 8, y + 58, true);
        }

        // Inner circle
        int cx = x + 8;
        int cy = y + 175;
        g.setColor(new Color(160, 200, 160));
        g.setFont(new Font("SansSerif", Font.ITALIC, 9));
        g.drawString("Inner Circle:", x + 8, cy - 4);
        for (CharacterCard ch : p.getInnerCircle()) {
            if (cx + 50 > x + w - 4) break;
            drawMiniCard(g, ch, cx, cy, false);
            cx += 52;
        }

        // Fleets
        cx = x + 8;
        cy = y + 310;
        g.setColor(new Color(160, 180, 220));
        g.drawString("Fleets:", x + 8, cy - 4);
        for (FleetCard fl : p.getFleets()) {
            if (cx + 50 > x + w - 4) break;
            drawMiniCard(g, fl, cx, cy, false);
            cx += 52;
        }

        // Groups / Locations
        cx = x + 8;
        cy = y + 430;
        g.setColor(new Color(200, 180, 140));
        g.drawString("Groups/Loc:", x + 8, cy - 4);
        for (GroupCard gr : p.getGroups()) {
            if (cx + 50 > x + w - 4) break;
            drawMiniCard(g, gr, cx, cy, false);
            cx += 52;
        }
        for (LocationCard lc : p.getLocations()) {
            if (cx + 50 > x + w - 4) break;
            drawMiniCard(g, lc, cx, cy, false);
            cx += 52;
        }

        // Deck count
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString("Deck: " + p.getDeck().size()
            + "  Hand: " + p.getHand().size(), x + 8, y + h - 10);

        // Agenda
        if (p.getAgenda() != null) {
            g.setColor(new Color(220, 180, 60));
            g.setFont(new Font("SansSerif", Font.ITALIC, 9));
            String aName = p.getAgenda().getTitle();
            if (aName.length() > 18) aName = aName.substring(0, 17) + "…";
            g.drawString("Agenda: " + aName, x + 8, y + h - 22);
        }
    }

    private void drawMiniCard(Graphics2D g, Card card, int x, int y, boolean large) {
        int w = large ? 60 : 46;
        int h = large ? 84 : 64;

        Color bg = card.isFaceDown() ? new Color(40, 40, 70) : new Color(25, 25, 55);
        g.setColor(bg);
        g.fillRoundRect(x, y, w, h, 6, 6);

        g.setColor(card.isRotated() ? Color.ORANGE
                   : card.isFaceDown() ? Color.DARK_GRAY
                   : new Color(150, 110, 40));
        g.setStroke(new BasicStroke(1));
        g.drawRoundRect(x, y, w, h, 6, 6);

        if (!card.isFaceDown()) {
            String abbr = card.getTitle().length() > 12
                ? card.getTitle().substring(0, 11) + "…" : card.getTitle();
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.PLAIN, 7));
            g.drawString(abbr, x + 2, y + 10);

            g.setFont(new Font("SansSerif", Font.BOLD, 8));
            g.setColor(new Color(160, 220, 160));
            if (card instanceof CharacterCard) {
                CharacterCard ch = (CharacterCard) card;
                g.drawString("D" + ch.getDiplomacy() + " I" + ch.getIntrigue(), x + 2, y + h - 18);
                g.drawString("P" + ch.getPsi() + " L" + ch.getLeadership(), x + 2, y + h - 8);
            } else if (card instanceof FleetCard) {
                g.setColor(new Color(160, 180, 220));
                g.drawString("MIL:" + ((FleetCard) card).getMilitary(), x + 2, y + h - 8);
            } else if (card instanceof LocationCard) {
                g.setColor(new Color(220, 200, 120));
                g.drawString("+" + ((LocationCard) card).getInfluencePerRound() + " INF", x + 2, y + h - 8);
            }
        }
    }

    private Color factionColor(Player p) {
        switch (p.getFaction()) {
            case HUMAN:    return new Color(100, 160, 220);
            case MINBARI:  return new Color(180, 150, 220);
            case CENTAURI: return new Color(220, 200, 100);
            case NARN:     return new Color(220, 120, 80);
            default:       return Color.WHITE;
        }
    }
}
