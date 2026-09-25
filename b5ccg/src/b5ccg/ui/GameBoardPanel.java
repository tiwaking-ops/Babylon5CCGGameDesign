package b5ccg.ui;

import b5ccg.model.*;
import b5ccg.model.enums.ConflictType;
import b5ccg.model.enums.TensionMatrix;
// B5-0429: tension readout iterates the matrix's Faction keys directly.
import b5ccg.model.enums.Faction;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class GameBoardPanel extends JPanel {

    private GameState state;

    // B5-0423: board-side selection. Mirrors HandPanel's callback; feeds the
    // board-zone controls (heal/repair/lead-fleet/use-rotate-effect) with the
    // IC / supporting / fleet / location cards that never appear in hand.
    private Card selectedCard;
    private CardSelectedListener onBoardCardSelected;

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
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    /** B5-0361: the UI-held conflict (active, or the last one before the
     *  controller cleared it). MainWindow brokers it to HandPanel so the
     *  eligible-aftermath highlight can consult the real 6-arg legality. */
    public Conflict getLastHeldConflict() { return lastHeldConflict; }

    /**
     * B5-0423: board click. Mirrors HandPanel.handleClick — a hit on a board
     * card selects it locally and notifies the controller; a miss only drops
     * the local highlight (the controller's own selection is untouched, so a
     * hand selection survives a stray click on empty board space).
     */
    private void handleClick(int mx, int my) {
        Card hit = resolveCardAt(mx, my);
        selectedCard = hit;
        if (hit != null && onBoardCardSelected != null) {
            onBoardCardSelected.onCardSelected(hit);
        }
        repaint();
    }

    /**
     * B5-0423: translate a panel point to the board card drawn under it. The
     * geometry MUST stay in lockstep with drawZone: one zone per player,
     * zoneW = getWidth() / player count, and each row's card origin/step
     * copied verbatim from its drawMiniCard call. Rows are probed top-down so
     * an overlap resolves to the higher band, matching what the eye sees.
     * Face-down cards are skipped — their identity is hidden from the host.
     */
    private Card resolveCardAt(int mx, int my) {
        if (state == null) return null;
        List<Player> players = state.getPlayers();
        if (players.isEmpty()) return null;
        int zoneW = getWidth() / players.size();
        if (zoneW <= 0) return null;
        int zoneIndex = mx / zoneW;
        if (zoneIndex < 0 || zoneIndex >= players.size()) return null;
        Player p = players.get(zoneIndex);
        int x = zoneIndex * zoneW;
        int y = 0;

        // Ambassador: 60x84 large mini-card at (x+8, y+58).
        if (hit(mx, my, x + 8, y + 58, 60, 84)) {
            Card amb = p.getAmbassador();
            if (amb != null && !amb.isFaceDown()) return amb;
            return null;
        }

        // Inner Circle: 46x64 at (x+8 + 52*i, y+175).
        int cx = x + 8;
        int cy = y + 175;
        for (CharacterCard ch : p.getInnerCircle()) {
            if (cx + 50 > x + zoneW - 4) break;
            if (hit(mx, my, cx, cy, 46, 64)) {
                return ch.isFaceDown() ? null : ch;
            }
            cx += 52;
        }

        // Supporting role: 46x16 chips at (x+8 + 50*i, y+269).
        int sx = x + 8;
        for (CharacterCard sch : p.getSupportingRole()) {
            if (sx + 46 > x + zoneW - 4) break;
            if (hit(mx, my, sx, y + 269, 46, 16)) {
                return sch.isFaceDown() ? null : sch;
            }
            sx += 50;
        }

        // Fleets: 46x64 at (x+8 + 52*i, y+310).
        cx = x + 8;
        cy = y + 310;
        for (FleetCard fl : p.getFleets()) {
            if (cx + 50 > x + zoneW - 4) break;
            if (hit(mx, my, cx, cy, 46, 64)) {
                return fl.isFaceDown() ? null : fl;
            }
            cx += 52;
        }

        // Groups then Locations share one band: 46x64 at (x+8 + 52*i, y+430).
        cx = x + 8;
        cy = y + 430;
        for (GroupCard gr : p.getGroups()) {
            if (cx + 50 > x + zoneW - 4) break;
            if (hit(mx, my, cx, cy, 46, 64)) {
                return gr.isFaceDown() ? null : gr;
            }
            cx += 52;
        }
        for (LocationCard lc : p.getLocations()) {
            if (cx + 50 > x + zoneW - 4) break;
            if (hit(mx, my, cx, cy, 46, 64)) {
                return lc.isFaceDown() ? null : lc;
            }
            cx += 52;
        }
        return null;
    }

    /** B5-0423: point-in-rect test for a card origin. */
    private boolean hit(int mx, int my, int rx, int ry, int rw, int rh) {
        return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
    }

    /**
     * B5-0423: highlight the selected card. A 2px lime stroke inset over the
     * mini-card; the `large` form carries the ambassador's 60x84 footprint.
     * Identity comparison is by reference so two copies of one card title
     * never cross-highlight.
     */
    private void drawSelectionMark(Graphics2D g, Card card, int x, int y) {
        drawSelectionMark(g, card, x, y, false);
    }

    private void drawSelectionMark(Graphics2D g, Card card, int x, int y, boolean large) {
        if (card == null || card != selectedCard) return;
        int w = large ? 60 : 46;
        int h = large ? 84 : 64;
        g.setColor(new Color(120, 255, 120));
        g.setStroke(new BasicStroke(2.0f));
        g.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 6, 6);
    }

    /** B5-0423: board-side selection callback (mirror of HandPanel.set). */
    public void setOnBoardCardSelected(CardSelectedListener cb) { onBoardCardSelected = cb; }

    /** B5-0423: drop the local highlight after a submit. */
    public void clearSelection() {
        selectedCard = null;
        repaint();
    }

    public void update(GameState s) {
        this.state = s;
        selectedCard = null;   // highlight is transient per-refresh (mirrors HandPanel)
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

        // B5-0427: board-level at-war faction-pairs line, read from the
        // B5-0376 tension matrix. Rendered only when some pair is at war —
        // the normal pool state has no tension sources, so this stays clear.
        String warLine = atWarLine();
        if (warLine.length() > 0) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics wfm = g2.getFontMetrics();
            int lw = wfm.stringWidth(warLine);
            int wy = getHeight() - 40;
            g2.setColor(new Color(120, 10, 10, 200));
            g2.fillRoundRect((getWidth() - lw) / 2 - 4, wy - 10, lw + 8, 14, 5, 5);
            g2.setColor(new Color(255, 170, 170));
            g2.drawString(warLine, (getWidth() - lw) / 2, wy);
        }

        // B5-0429: station + non-player-forces readout (B5-0340 model API),
        // always rendered — the ratings exist from round one even while inert.
        String station = stationLine();
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        FontMetrics sfm = g2.getFontMetrics();
        int sx = (getWidth() - sfm.stringWidth(station)) / 2;
        int sy = getHeight() - 24;
        g2.setColor(new Color(150, 190, 230));
        g2.drawString(station, sx, sy);
        if (state.isShadowWar()) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.setColor(new Color(255, 120, 120));
            g2.drawString("  [SHADOW WAR]", sx + sfm.stringWidth(station), sy);
        }

        // B5-0429: directional tension pairs (B5-0376), only when any pair
        // is nonzero — the pool has no tension sources, so this stays clear
        // in normal games. Drawn above the war pill band to keep the bottom
        // rows distinct from the per-zone deck lines.
        String tension = tensionLine();
        if (tension.length() > 0) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            FontMetrics tfm = g2.getFontMetrics();
            g2.setColor(new Color(230, 170, 120));
            g2.drawString(tension, (getWidth() - tfm.stringWidth(tension)) / 2,
                          getHeight() - 58);
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

    /** B5-0429: station + shadow/vorlon readout line from the B5-0340 API
     *  (getStation().getInfluence(), getShadowInfluence(), getVorlonInfluence()).
     *  The [SHADOW WAR] marker is appended by the painter, not here, so the
     *  string stays a pure data readout. Public for headless probes. */
    public String stationLine() {
        if (state == null) return "";
        return "Station: " + state.getStation().getInfluence()
             + "  |  Shadow " + state.getShadowInfluence()
             + "  Vorlon " + state.getVorlonInfluence();
    }

    /** B5-0429: directional tension pairs readout (B5-0376), sorted for
     *  deterministic order, one label per nonzero directed pair.
     *  Empty string when every pair is zero. Snapshot-first per the paint-
     *  thread rule (getTensionMap is a live-map view). Public for probes. */
    public String tensionLine() {
        if (state == null) return "";
        java.util.Map<Faction, java.util.Map<Faction, Integer>> map =
            state.getTensionMatrix().getTensionMap();
        java.util.ArrayList<String> labels = new java.util.ArrayList<String>();
        java.util.ArrayList<Faction> sources =
            new java.util.ArrayList<Faction>(map.keySet());
        for (Faction src : sources) {
            java.util.Map<Faction, Integer> row = map.get(src);
            java.util.ArrayList<Faction> targets =
                new java.util.ArrayList<Faction>(row.keySet());
            for (Faction tgt : targets) {
                Integer v = row.get(tgt);
                if (v != null && v.intValue() > 0) {
                    labels.add(src.toString() + " to " + tgt.toString() + " " + v);
                }
            }
        }
        if (labels.isEmpty()) return "";
        java.util.Collections.sort(labels);
        StringBuilder sb = new StringBuilder("Tension: ");
        for (int i = 0; i < labels.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(labels.get(i));
        }
        return sb.toString();
    }

    /** B5-0427: board-level at-war pairs line from the B5-0376 tension
     *  matrix (getAtWarPairs snapshot, sorted for deterministic order).
     *  Empty string when no pair is at war. Public so a headless paint
     *  probe can assert the readout without parsing pixels. */
    public String atWarLine() {
        if (state == null) return "";
        java.util.Set<TensionMatrix.FactionPair> pairs =
            state.getTensionMatrix().getAtWarPairs();
        if (pairs.isEmpty()) return "";
        java.util.ArrayList<String> labels = new java.util.ArrayList<String>();
        for (TensionMatrix.FactionPair fp : pairs) {
            labels.add(fp.a.toString() + "-" + fp.b.toString());
        }
        java.util.Collections.sort(labels);
        StringBuilder sb = new StringBuilder("At war: ");
        for (int i = 0; i < labels.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(labels.get(i));
        }
        return sb.toString();
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
            drawSelectionMark(g, p.getAmbassador(), x + 8, y + 58, true);
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
            drawSelectionMark(g, ch, cx, cy);
            cx += 52;
        }
        // B5-0330a: note below the card band — the draft drew it inside the
        // 64px mini-card, colliding with the first card's stats.
        overflowNote(g, p.getInnerCircle().size() - icDrawn, x, cy);

        // B5-0423: supporting-role chip row — supporting chars render nowhere
        // else, so Use Rotate Effect / Promote had no selectable source. A
        // compact 46x16 chip band sits between the IC row (ends y+239) and the
        // fleets header (y+306), clear of both.
        java.util.List<CharacterCard> supp = p.getSupportingRole();
        if (!supp.isEmpty()) {
            g.setFont(new Font("SansSerif", Font.ITALIC, 9));
            g.setColor(new Color(170, 210, 170));
            g.drawString("Support:", x + 8, y + 265);
            int sx = x + 8;
            for (CharacterCard sch : supp) {
                if (sx + 46 > x + w - 4) break;
                boolean sel = sch == selectedCard;
                g.setColor(sel ? new Color(30, 90, 40) : new Color(30, 50, 30));
                g.fillRoundRect(sx, y + 269, 46, 16, 5, 5);
                g.setColor(sel ? new Color(120, 240, 120) : new Color(120, 150, 120));
                g.setStroke(new BasicStroke(sel ? 2 : 1));
                g.drawRoundRect(sx, y + 269, 46, 16, 5, 5);
                String label = sch.getTitle();
                if (label.length() > 9) label = label.substring(0, 8) + "…";
                g.setFont(new Font("SansSerif", Font.PLAIN, 7));
                g.setColor(sel ? new Color(180, 255, 180) : Color.WHITE);
                g.drawString(label, sx + 3, y + 280);
                sx += 50;
            }
        }

        // Fleets
        cx = x + 8;
        cy = y + 310;
        g.setColor(new Color(160, 180, 220));
        g.drawString("Fleets:", x + 8, cy - 4);
        int flDrawn = 0;
        for (FleetCard fl : p.getFleets()) {
            if (cx + 50 > x + w - 4) break;
            drawMiniCard(g, fl, cx, cy, false);
            drawSelectionMark(g, fl, cx, cy);
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
                drawSelectionMark(g, gr, cx, cy);
                cx += 52;
                glDrawn++;
            }
            for (LocationCard lc : p.getLocations()) {
                if (cx + 50 > x + w - 4) break;
                drawMiniCard(g, lc, cx, cy, false);
                drawSelectionMark(g, lc, cx, cy);
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
                // B5-0427: captured-by + effects-suppressed readout from the
                // B5-0376 model API (getCapturedBy, isEffectsSuppressed).
                // Face-up only: a face-down card's identity stays hidden.
                LocationCard loc = (LocationCard) card;
                String capBy = loc.getCapturedBy() != null
                    ? loc.getCapturedBy().getName() : null;
                if (capBy != null && capBy.length() > 6) capBy = capBy.substring(0, 6);
                String mark = (capBy != null ? "CAP:" + capBy : "")
                            + (loc.isEffectsSuppressed()
                               ? (capBy != null ? " " : "") + "SUP" : "");
                if (mark.length() > 0) {
                    g.setColor(new Color(255, 130, 130));
                    g.setFont(new Font("SansSerif", Font.BOLD, 7));
                    g.drawString(mark, x + 2, y + h - 18);
                }
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
