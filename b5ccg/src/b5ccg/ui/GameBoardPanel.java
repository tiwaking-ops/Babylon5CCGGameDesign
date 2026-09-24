package b5ccg.ui;

import b5ccg.model.*;
import b5ccg.model.enums.ConflictType;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GameBoardPanel extends JPanel {

    private GameState state;

    // B5-0347: outcome banner state. The controller clears the active conflict
    // right after resolution (GameController.resolveCurrentConflict), so the
    // UI holds the last Conflict object: after resolve(winner) it retains
    // isResolved()/getWinner()/side totals — structured outcome data, no log
    // parsing. Shown until a new conflict activates.
    private Conflict lastHeldConflict;
    private String   lastOutcomeTitle;
    private String   lastOutcomeDetail;

    public GameBoardPanel() {
        setBackground(new Color(15, 35, 15));
        setPreferredSize(new Dimension(1280, 560));
    }

    /** B5-0361: the UI-held conflict (active, or the last one before the
     *  controller cleared it). MainWindow brokers it to HandPanel so the
     *  eligible-aftermath highlight can consult the real 6-arg legality. */
    public Conflict getLastHeldConflict() { return lastHeldConflict; }

    public void update(GameState s) {
        this.state = s;
        Conflict active = s.getActiveConflict();
        if (active != null) {
            if (active != lastHeldConflict) {   // a new conflict activated
                lastHeldConflict = active;
                lastOutcomeTitle = null;
                lastOutcomeDetail = null;
            }
        } else if (lastHeldConflict != null) {
            if (lastHeldConflict.isResolved() && lastHeldConflict.getWinner() != null) {
                lastOutcomeTitle  = lastHeldConflict.getCard().getTitle()
                                    + "  —  WON BY " + lastHeldConflict.getWinner().getName();
                lastOutcomeDetail = "support=" + lastHeldConflict.supportTotal()
                                    + "  opposition=" + lastHeldConflict.oppositionTotal()
                                    + "  (" + lastHeldConflict.getInfluenceReward()
                                    + " influence to the winner)";
            } else {
                // cleared without resolution (round advance): drop it
                lastHeldConflict = null;
                lastOutcomeTitle = null;
                lastOutcomeDetail = null;
            }
        }
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
            // B5-0329a: the "assistant status overlay" that stood here was
            // removed — it invented five personas and fake task references for
            // the rulebook §IV assistant mechanic, which is not implemented
            // anywhere in model/ (its 8pt font also corrupted this banner).
            String msg = c.isWarConflict()
                ? "WAR: " + warConflictTitle(c)
                : "CONFLICT: " + c.getCard().getTitle()
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

            // Per-participant breakdown (B5-0346, D14 decision 2): committed
            // cards per side, under the sides readout. Read-only render of the
            // D14 sides API. Card lists are snapshotted before iteration so a
            // commit landing mid-paint cannot throw a
            // ConcurrentModificationException on the paint thread; iteration
            // goes through state.getPlayers() for a deterministic order.
            java.util.ArrayList<Player> roster = new java.util.ArrayList<Player>(state.getPlayers());
            int lineY = (getHeight() / 2) + 62;
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            FontMetrics detailFm = g.getFontMetrics();
            for (Player pl : roster) {
                boolean support = c.isSupporting(pl);
                boolean oppose  = c.isOpposing(pl);
                if (!support && !oppose) continue;   // not a participant
                java.util.ArrayList<Card> cards =
                    new java.util.ArrayList<Card>(c.getCommittedCards(pl));
                StringBuilder line = new StringBuilder();
                line.append(support ? "[support] " : "[oppose]  ");
                line.append(pl.getName()).append(": ");
                if (cards.isEmpty()) {
                    line.append("(no cards committed)");
                } else {
                    for (int i = 0; i < cards.size(); i++) {
                        if (i > 0) line.append(", ");
                        line.append(cards.get(i).getTitle());
                    }
                    line.append("  (total ").append(c.playerTotal(pl)).append(")");
                }
                String detail = line.toString();
                g.setColor(support ? new Color(120, 200, 120) : new Color(220, 160, 120));
                g.drawString(detail, (getWidth() - detailFm.stringWidth(detail)) / 2, lineY);
                lineY += 13;
            }
            }   // closes the active-conflict block

            // B5-0347: conflict outcome banner — SIBLING of the active-conflict
            // block: it must render exactly when the active conflict is gone
            // (the controller clears it right after resolution). Placing it
            // inside the active block made it unreachable in its only useful
            // state — caught by the reflection + pixel diagnostic.
            if (lastOutcomeTitle != null) {
                FontMetrics ofm = g2.getFontMetrics();
                g2.setColor(new Color(40, 120, 40, 210));
                g2.fillRoundRect(getWidth() / 4, getHeight() / 2 - 20, getWidth() / 2, 40, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                g2.drawString(lastOutcomeTitle,
                    (getWidth() - ofm.stringWidth(lastOutcomeTitle)) / 2, getHeight() / 2 + 2);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.setColor(new Color(200, 230, 200));
                g2.drawString(lastOutcomeDetail,
                    (getWidth() - ofm.stringWidth(lastOutcomeDetail)) / 2, getHeight() / 2 + 16);
            }
    }

    /** B5-0376: banner title for a war conflict — the declared kind plus its
     *  target (race or location), since a war conflict carries no ConflictCard. */
    private String warConflictTitle(Conflict c) {
        if (c.getTargetLocation() != null) {
            return "war on " + c.getTargetLocation().getTitle();
        }
        Player t = c.getTarget();
        return "war on " + (t != null ? t.getName() : "(unknown)") + "  [MILITARY]";
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
            // B5-0339 assistant state (F10, real mechanic): A+ while the
            // assist bonus is live (assistant rotated), A$ while a sponsor
            // discount is pending. Markers sit over the mini-card's corner.
            boolean ab = p.getAmbassador().isAssistantBonus();
            boolean sd = p.getSponsorDiscount() > 0;
            if (ab || sd) {
                String mark = (ab ? "A+" : "") + (sd ? "A$" : "");
                g.setColor(new Color(255, 210, 90));
                g.setFont(new Font("SansSerif", Font.BOLD, 9));
                g.drawString(mark, x + 8 + 46, y + 58 + 12);
            }
        }

        // B5-0347: agenda slot state + attached aftermaths (B5-0338 registry).
        // Line sits between the IC band and the fleets band, clear of both.
        boolean hasAgenda = p.getAgenda() != null;
        // B5-0346 lesson: getAttachedAftermaths returns the LIVE internal list
        // — snapshot before iterating on the paint thread.
        java.util.List<AftermathCard> att =
            new java.util.ArrayList<AftermathCard>(state.getAttachedAftermaths(p));
        if (hasAgenda || !att.isEmpty()) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 9));
            // B5-0347 probe finding: y+268 sits UNDER the center conflict/outcome
            // banner rect (x w/4..3w/4, y262..296, drawn after zones) — zone 2+ lines
            // were occluded exactly while a conflict banner was up. Draw below the
            // banner band, above the per-participant breakdown lines (y342+).
            int ly = y + 308;
            if (hasAgenda) {
                AgendaCard agenda = p.getAgenda();
                String slot = agenda.isMajorAgenda() ? "MAJOR" : "minor";
                String agendaName = agenda.isFaceDown() ? "[face-down]" : agenda.getTitle();
                String agendaLine = "Agenda (" + slot + "): " + agendaName;
                g.setColor(new Color(220, 180, 60));
                g.drawString(agendaLine, x + 8, ly);
                if (!agenda.isFaceDown() && agenda.isConditionMet(state, p)) {
                    String winMark = " [WIN]";
                    g.setColor(new Color(120, 240, 120));
                    g.drawString(winMark, x + 8 + g.getFontMetrics().stringWidth(agendaLine), ly);
                }
                ly += 11;
            }
            if (!att.isEmpty()) {
                g.setColor(new Color(170, 170, 230));
                String amLine = "Aftermaths attached: " + att.size();
                for (int i = 0; i < att.size() && i < 3; i++) {
                    amLine += ", " + att.get(i).getTitle();
                }
                if (att.size() > 3) amLine += " (…";
                g.drawString(amLine, x + 8, ly);
            }
        }

        // Inner circle
        int cx = x + 8;
        int cy = y + 175;
        g.setColor(new Color(160, 200, 160));
        g.setFont(new Font("SansSerif", Font.ITALIC, 9));
        g.drawString("Inner Circle:", x + 8, cy - 4);
        int icDrawn = 0;
        for (CharacterCard ch : p.getInnerCircle()) {
            if (cx + 50 > x + w - 4) break;
            drawMiniCard(g, ch, cx, cy, false);
            cx += 52;
            icDrawn++;
        }
        // B5-0330a: note below the card band — the draft drew it inside the
        // 64px mini-card, colliding with the first card's stats.
        overflowNote(g, p.getInnerCircle().size() - icDrawn, x, cy);

        // Fleets
        cx = x + 8;
        cy = y + 310;
        g.setColor(new Color(160, 180, 220));
        g.drawString("Fleets:", x + 8, cy - 4);
        int flDrawn = 0;
        for (FleetCard fl : p.getFleets()) {
            if (cx + 50 > x + w - 4) break;
            drawMiniCard(g, fl, cx, cy, false);
            cx += 52;
            flDrawn++;
        }
        overflowNote(g, p.getFleets().size() - flDrawn, x, cy);

        // Groups / Locations
        cx = x + 8;
        cy = y + 430;
        g.setColor(new Color(200, 180, 140));
        g.drawString("Groups/Loc:", x + 8, cy - 4);
        int glDrawn = 0;
        int glTotal = p.getGroups().size() + p.getLocations().size();
        for (GroupCard gr : p.getGroups()) {
            if (cx + 50 > x + w - 4) break;
            drawMiniCard(g, gr, cx, cy, false);
            cx += 52;
            glDrawn++;
        }
        for (LocationCard lc : p.getLocations()) {
            if (cx + 50 > x + w - 4) break;
            drawMiniCard(g, lc, cx, cy, false);
            cx += 52;
            glDrawn++;
        }
        overflowNote(g, glTotal - glDrawn, x, cy);

        // Deck count
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString("Deck: " + p.getDeck().size()
            + "  Hand: " + p.getHand().size(), x + 8, y + h - 10);

        // Agenda
        if (p.getAgenda() != null) {
            g.setColor(new Color(220, 180, 60));
            g.setFont(new Font("SansSerif", Font.ITALIC, 9));
            String aName = p.getAgenda().isFaceDown()
                ? "[face-down]" : p.getAgenda().getTitle();
            if (aName.length() > 18) aName = aName.substring(0, 17) + "…";
            g.drawString("Agenda: " + aName, x + 8, y + h - 22);
        }
    }

    /**
     * B5-0330a: shared "(+ N more)" overflow note, drawn BELOW the row's card
     * band. Mini-cards occupy y0 .. y0+64 and their stat baselines sit at
     * y0+48 / y0+56 (drawMiniCard), so the note's baseline sits at y0+80 —
     * below the card border stroke plus descender clearance. No-op when
     * nothing is hidden.
     */
    private void overflowNote(Graphics2D g, int hiddenCount, int x, int y0) {
        if (hiddenCount <= 0) return;
        g.setColor(new Color(200, 180, 100));
        g.setFont(new Font("SansSerif", Font.PLAIN, 8));
        g.drawString("(+ " + hiddenCount + " more)", x + 8, y0 + 80);
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

        // B5-0381: the model intentionally exposes a host's contingency count
        // without exposing the face-down card identities. Keep the board
        // readout equally opaque while showing that cards are attached here.
        int contingencyCount = card.getContingencyCount();
        if (contingencyCount > 0) {
            String marker = "C" + contingencyCount;
            g.setColor(new Color(255, 220, 120));
            g.setFont(new Font("SansSerif", Font.BOLD, 8));
            g.drawString(marker, x + w - g.getFontMetrics().stringWidth(marker) - 2,
                         y + h - 2);
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
