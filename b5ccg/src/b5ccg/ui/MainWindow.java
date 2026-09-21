package b5ccg.ui;

import b5ccg.engine.GameController;
import b5ccg.engine.RulesEngine;
import b5ccg.model.*;
import b5ccg.model.enums.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainWindow extends JFrame {

    private final GameController  controller;
    private final GameBoardPanel  boardPanel;
    private final HandPanel       handPanel;
    private final JTextArea       logArea;
    private final JLabel          statusLabel;
    private final JButton         passButton;
    private final JButton         playCardButton;

    // B5-0326 F3: action-set buttons
    private JButton sponsorButton;
    private JButton promoteButton;
    private JButton buildInfluenceButton;

    // B5-0326 F5: cost preview readout
    private JLabel  costLabel;

    // B5-0325 F2: conflict target selector
    private JComboBox<String> targetSelector;
    private JButton supportButton;
    private JButton opposeButton;
    private Player selectedTarget;

    private Card selectedCard;
    private RulesEngine rules;

    public MainWindow(GameController controller) {
        super("Babylon 5 CCG — Single Player");
        this.controller = controller;
        this.rules = new RulesEngine();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(4, 4));
        getContentPane().setBackground(new Color(10, 20, 10));

        // ── Board ─────────────────────────────────────────────────────────────
        boardPanel = new GameBoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // ── Right sidebar: log ────────────────────────────────────────────────
        logArea = new JTextArea(12, 28);
        logArea.setEditable(false);
        logArea.setBackground(new Color(10, 15, 30));
        logArea.setForeground(new Color(180, 200, 180));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 120, 80)),
            "Game Log", 0, 0, new Font("SansSerif", Font.BOLD, 10),
            new Color(180, 200, 180)));
        logScroll.getViewport().setBackground(new Color(10, 15, 30));
        add(logScroll, BorderLayout.EAST);

        // ── Bottom toolbar ────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setBackground(new Color(10, 20, 10));

        statusLabel = new JLabel("Initialising…");
        statusLabel.setForeground(new Color(200, 220, 200));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        passButton = makeButton("Pass Turn", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainWindow.this.controller.submitHumanAction(GameAction.pass());
            }
        });
        playCardButton = makeButton("Play / Initiate", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playSelected();
            }
        });
        playCardButton.setEnabled(false);

        // B5-0326 F3: sponsor / promote / build-influence buttons
        sponsorButton = makeButton("Sponsor", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CharacterCard ch = (selectedCard instanceof CharacterCard)
                    ? (CharacterCard) selectedCard : null;
                if (ch != null && rules.canRecruit(humanPlayer(), ch)) {
                    MainWindow.this.controller.submitHumanAction(
                        GameAction.recruitCharacter(ch));
                }
            }
        });
        sponsorButton.setEnabled(false);

        promoteButton = makeButton("Promote", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CharacterCard ch = (selectedCard instanceof CharacterCard)
                    ? (CharacterCard) selectedCard : null;
                if (ch != null) {
                    Player hp = humanPlayer();
                    CharacterCard leader = findUnrotatedIC(hp);
                    if (rules.canPromote(hp, ch) && leader != null) {
                        MainWindow.this.controller.submitHumanAction(
                            GameAction.promoteCharacter(ch, leader));
                    }
                }
            }
        });
        promoteButton.setEnabled(false);

        buildInfluenceButton = makeButton("Build Influence", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player hp = humanPlayer();
                CharacterCard leader = findUnrotatedIC(hp);
                if (leader != null && rules.canBuildInfluence(hp)) {
                    MainWindow.this.controller.submitHumanAction(
                        GameAction.buildInfluence(leader));
                }
            }
        });
        buildInfluenceButton.setEnabled(false);

        // B5-0325 F2: conflict target selector
        targetSelector = new JComboBox<String>();
        targetSelector.setEnabled(false);
        targetSelector.setMaximumSize(new Dimension(180, 24));
        targetSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (targetSelector.getSelectedItem() != null) {
                    String name = (String) targetSelector.getSelectedItem();
                    for (Player p : MainWindow.this.controller.getState().getPlayers()) {
                        if (!p.isHuman() && p.getName().equals(name)) {
                            selectedTarget = p;
                            break;
                        }
                    }
                    playCardButton.setEnabled(selectedTarget != null
                        && !(selectedCard instanceof ConflictCard
                             && MainWindow.this.controller.getState().getActiveConflict() != null));
                }
            }
        });

        // B5-0325 F1: support / oppose join buttons
        supportButton = makeButton("Support", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainWindow.this.controller.submitHumanAction(GameAction.joinSupport());
            }
        });
        supportButton.setEnabled(false);
        opposeButton = makeButton("Oppose", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainWindow.this.controller.submitHumanAction(GameAction.joinOppose());
            }
        });
        opposeButton.setEnabled(false);

        // B5-0326 F5: cost preview readout
        costLabel = new JLabel("  ");
        costLabel.setForeground(new Color(200, 220, 200));
        costLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));

        // Assemble toolbar
        toolbar.add(passButton);
        toolbar.add(playCardButton);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(sponsorButton);
        toolbar.add(promoteButton);
        toolbar.add(buildInfluenceButton);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(costLabel);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(targetSelector);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(supportButton);
        toolbar.add(opposeButton);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(statusLabel);

        add(toolbar, BorderLayout.NORTH);

        // ── Hand ──────────────────────────────────────────────────────────────
        handPanel = new HandPanel();
        final JLabel fStatusLabel = statusLabel;
        handPanel.setOnCardSelected(new CardSelectedListener() {
            @Override
            public void onCardSelected(Card card) {
                selectedCard = card;
                if (card instanceof ConflictCard) {
                    // B5-0325 F2: populate target selector with non-human players
                    targetSelector.removeAllItems();
                    for (Player p : MainWindow.this.controller.getState().getPlayers()) {
                        if (!p.isHuman()) {
                            targetSelector.addItem(p.getName());
                        }
                    }
                    targetSelector.setEnabled(true);
                    fStatusLabel.setText("Selected: " + card.getTitle()
                        + "  |  Target: choose from dropdown");
                } else {
                    targetSelector.setEnabled(false);
                    fStatusLabel.setText("Selected: " + card.getTitle()
                        + "  |  " + card.getText());
                }
                playCardButton.setEnabled(true);
                // B5-0326 F5: refresh cost preview immediately
                refreshCostPreview();
            }
        });
        add(handPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1300, 820));
    }

    private Player humanPlayer() {
        return MainWindow.this.controller.getState().getHumanPlayer();
    }

    private CharacterCard findUnrotatedIC(Player p) {
        for (CharacterCard ch : p.getInnerCircle()) {
            if (!ch.isRotated()) return ch;
        }
        return null;
    }

    /** B5-0326 F5: show preview of the cost for the currently selected card. */
    private void refreshCostPreview() {
        CharacterCard ch = (selectedCard instanceof CharacterCard)
            ? (CharacterCard) selectedCard : null;
        Player hp = humanPlayer();
        if (ch == null || hp == null) {
            costLabel.setText("  ");
            return;
        }
        GamePhase phase = MainWindow.this.controller.getState().getPhase();
        if (phase != GamePhase.ACTION) {
            costLabel.setText("  ");
            return;
        }
        StringBuilder sb = new StringBuilder();
        // Sponsor (RECRUIT) cost
        if (hp.getHand().contains(ch) && !ch.isRotated() && !ch.isFaceDown()) {
            int rc = rules.recruitCost(hp, ch);
            sb.append("Sponsor cost: " + rc + "  |  ");
        }
        // Promote cost
        if (hp.getSupportingRole().contains(ch) && !ch.isRotated() && !ch.isFaceDown()) {
            int pc = rules.promotionCost(hp, ch);
            sb.append("Promote cost: " + pc + "  |  ");
        }
        // Build Influence — not card-specific, but show if available
        if (rules.canBuildInfluence(hp)) {
            sb.append("Build Inf: -3 (+1 rating)  |  ");
        }
        if (sb.length() == 0) {
            costLabel.setText("  ");
        } else {
            // trim trailing "  |  "
            String txt = sb.toString();
            if (txt.endsWith("  |  ")) txt = txt.substring(0, txt.length() - 6);
            costLabel.setText("  " + txt);
        }
    }

    /** Called from the game controller (off EDT) whenever state changes. */
    public void onStateUpdate(final GameState state) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { refresh(state); }
        });
    }

    private void refresh(GameState state) {
        boardPanel.update(state);
        Player human = state.getHumanPlayer();
        handPanel.update(human.getHand());

        // Append new log lines
        java.util.List<String> log = state.getLog();
        logArea.setText("");
        for (String line : log) logArea.append(line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());

        boolean myTurn = state.getActivePlayer() == human
            && MainWindow.this.controller.isWaitingForHuman();
        boolean activeConflict = state.getActiveConflict() != null;
        GamePhase phase = state.getPhase();

        // B5-0325 F1: support/oppose buttons enabled during active conflict
        supportButton.setEnabled(activeConflict);
        opposeButton.setEnabled(activeConflict);

        passButton.setEnabled(myTurn);

        // Play button: enabled on my turn with a selected card.
        // If a conflict card is selected and there's already an active
        // conflict, disable (can't start a second conflict).
        if (selectedCard instanceof ConflictCard && activeConflict) {
            playCardButton.setEnabled(false);
        } else {
            playCardButton.setEnabled(myTurn && selectedCard != null);
        }

        // B5-0326 F3: per-phase enablement for action-set buttons
        boolean actionPhase = (phase == GamePhase.ACTION);
        CharacterCard ch = (selectedCard instanceof CharacterCard)
            ? (CharacterCard) selectedCard : null;

        // Sponsor: ACTION phase, my turn, selected card is a ready character in hand
        sponsorButton.setEnabled(actionPhase && myTurn
            && ch != null && ch instanceof CharacterCard
            && !ch.isFaceDown() && !ch.isRotated()
            && human.getHand().contains(ch));

        // Promote: ACTION phase, my turn, selected card is a ready supporting-role char
        promoteButton.setEnabled(actionPhase && myTurn
            && ch != null && ch instanceof CharacterCard
            && !ch.isFaceDown() && !ch.isRotated()
            && human.getSupportingRole().contains(ch)
            && findUnrotatedIC(human) != null);

        // Build Influence: ACTION phase, my turn, has unrotated IC, influence <= 9
        buildInfluenceButton.setEnabled(actionPhase && myTurn
            && rules.canBuildInfluence(human));

        // B5-0326 F5: refresh cost preview
        refreshCostPreview();

        if (state.isGameOver()) {
            statusLabel.setText("GAME OVER — Winner: "
                + (state.getWinner() != null ? state.getWinner().getName() : "None"));
            passButton.setEnabled(false);
            playCardButton.setEnabled(false);
        } else {
            statusLabel.setText("Round " + state.getRoundNumber()
                + "  |  " + state.getPhase()
                + "  |  Active: " + state.getActivePlayer().getName()
                + (myTurn ? "  ← YOUR TURN" : ""));
        }
    }

    private void playSelected() {
        if (selectedCard == null) return;
        GameAction action;
        if (selectedCard instanceof ConflictCard) {
            // B5-0325 F2: use human-selected target, fall back to auto-target
            Player target = selectedTarget;
            if (target == null) {
                GameState state = MainWindow.this.controller.getState();
                int bestInfluence = -1;
                for (Player p : state.getPlayers()) {
                    if (!p.isHuman() && p.getInfluence() > bestInfluence) {
                        bestInfluence = p.getInfluence();
                        target = p;
                    }
                }
            }
            action = GameAction.initiateConflict(selectedCard, target);
        } else {
            action = GameAction.playCard(selectedCard);
        }
        selectedCard = null;
        selectedTarget = null;
        targetSelector.setSelectedIndex(-1);
        targetSelector.setEnabled(false);
        playCardButton.setEnabled(false);
        MainWindow.this.controller.submitHumanAction(action);
    }

    private JButton makeButton(String label, ActionListener al) {
        JButton btn = new JButton(label);
        btn.setBackground(new Color(40, 70, 40));
        btn.setForeground(new Color(200, 230, 200));
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 130, 80)),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        btn.addActionListener(al);
        return btn;
    }
}