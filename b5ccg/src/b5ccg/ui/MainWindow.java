package b5ccg.ui;

import b5ccg.engine.GameController;
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

    private Card selectedCard;
    private JComboBox<String> targetSelector;
    private JButton supportButton;
    private JButton opposeButton;
    private Player selectedTarget;

    public MainWindow(GameController controller) {
        super("Babylon 5 CCG — Single Player");
        this.controller = controller;

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

        // B5-0325 F2: conflict target selector
        targetSelector = new JComboBox<String>();
        targetSelector.setEnabled(false);
        targetSelector.setMaximumSize(new Dimension(180, 24));
        targetSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (targetSelector.getSelectedItem() != null) {
                    String name = (String) targetSelector.getSelectedItem();
                    for (Player p : controller.getState().getPlayers()) {
                        if (!p.isHuman() && p.getName().equals(name)) {
                            selectedTarget = p;
                            break;
                        }
                    }
                    playCardButton.setEnabled(selectedTarget != null
                        && !(selectedCard instanceof ConflictCard
                             && controller.getState().getActiveConflict() != null));
                }
            }
        });

        // B5-0325 F1: support / oppose join buttons
        supportButton = makeButton("Support", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.submitHumanAction(GameAction.joinSupport());
            }
        });
        supportButton.setEnabled(false);
        opposeButton = makeButton("Oppose", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.submitHumanAction(GameAction.joinOppose());
            }
        });
        opposeButton.setEnabled(false);

        // Assemble toolbar
        toolbar.add(passButton);
        toolbar.add(playCardButton);
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
                    for (Player p : controller.getState().getPlayers()) {
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
            }
        });
        add(handPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1300, 820));
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
            && controller.isWaitingForHuman();
        boolean activeConflict = state.getActiveConflict() != null;

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
                GameState state = controller.getState();
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
        controller.submitHumanAction(action);
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