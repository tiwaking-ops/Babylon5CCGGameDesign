package b5ccg.ui;

import b5ccg.engine.GameController;
import b5ccg.engine.RulesEngine;
import b5ccg.model.*;
import b5ccg.model.enums.*;
import java.util.List;
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
    private JButton playCardButton;

    // B5-0326 F3: action-set buttons
    private JButton sponsorButton;
    private JButton promoteButton;
    private JButton buildInfluenceButton;

    // B5-0380: agenda lifecycle actions for the human player's agenda slot.
    private JButton discardAgendaButton;
    private JButton replaceAgendaButton;
    private JButton revealAgendaButton;

// B5-0381: contingency reveal control. The human player gets a dropdown of
    // their placed (face-down) contingencies plus a Reveal button that submits
    // REVEAL_CONTINGENCY through the existing human-action pipeline.
    private JComboBox<String> contingencySelector;
    private JButton revealContingencyButton;

    // B5-0326 F5: cost preview readout
    private JLabel  costLabel;

    // B5-0348: hand filter/sort controls
    private JCheckBox typeCharBox;
    private JCheckBox typeFleetBox;
    private JCheckBox typeConflictBox;
    private JCheckBox typeAgendaBox;
    private JCheckBox typeAftermathBox;
    private JCheckBox typeEventBox;
    private JCheckBox typeEnhancementBox;
    private JCheckBox typeGroupBox;
    private JCheckBox typeLocationBox;
    private JCheckBox fascHumanBox;
    private JCheckBox fascMinbariBox;
    private JCheckBox fascCentauriBox;
    private JCheckBox fascNarnBox;
    private JCheckBox fascNeutralBox;
    private JCheckBox fascNonAlignedBox;
    private JCheckBox fascVorlonBox;
    private JCheckBox fascAnyBox;
    private JRadioButton sortUnsorted;
    private JRadioButton sortCostAsc;
    private JRadioButton sortCostDesc;
    private ButtonGroup sortGroup;
    private JCheckBox showUnplayable;

    // B5-0325 F2: conflict target selector
    private JComboBox<String> targetSelector;
    private JButton supportButton;
    private JButton opposeButton;
    private Player selectedTarget;

    // B5-0379: read-only participant list for the human join window. Rows are
    // snapshots of the active Conflict's D14 sides API (B5-0309); the panel
    // never mutates model state Ã¢â‚¬â€ Support/Oppose buttons remain the only
    // commit path (they submit JOIN_CONFLICT_* into the B5-0363 collect).
    private DefaultListModel participantListModel;
    private JList participantList;
    private JLabel joinPromptLabel;

    // B5-0327 F4: split Play/Initiate into separate buttons
    private JButton playCardOnlyButton;
    private JButton initiateConflictButton;

    // B5-0327 F8: initiative order display
    private JLabel initiativeLabel;

    // B5-0407: declare-war UI
    private JComboBox<String> warTargetSelector;
    private JButton declareWarButton;
    private JLabel warStatusLabel;

    // B5-0404: mercenary bid control
    private static final String[] BID_AMOUNTS = new String[] { "1", "2", "3", "5", "10" };
    private JComboBox<String> mercenaryBidAmountSelector;
    private JLabel mercenaryOfferLabel;
    private JLabel mercenaryControllerLabel;
    private JButton mercenaryBidButton;

    // B5-0401: Tier-1 remainder action UI — Lead Fleet + Use Rotate Effect
    private JButton leadFleetButton;
    private JComboBox<String> leadFleetSelector;
    private FleetCard selectedFleet;
    private JButton useRotateEffectButton;
    private JComboBox<String> rotateEffectKindSelector;
    private CharacterCard selectedAssistant;

    // B5-0402: Tier-3 action UI — Attack + Heal + Repair
    private JButton attackButton;
    private JButton healButton;
    private JButton repairButton;

    private Card selectedCard;
    private RulesEngine rules;

    public MainWindow(GameController controller) {
        super("Babylon 5 CCG Ã¢â‚¬â€ Single Player");
        this.controller = controller;
        this.rules = new RulesEngine();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(4, 4));
        getContentPane().setBackground(new Color(10, 20, 10));

        // Ã¢â€â‚¬Ã¢â€â‚¬ Board Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
        boardPanel = new GameBoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // Ã¢â€â‚¬Ã¢â€â‚¬ Right sidebar: log + conflict-type legend (F9) Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
        logArea = new JTextArea(10, 24);
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

        // B5-0379: right sidebar hosts the join-window participant list plus
        // the log and the legend. One container only Ã¢â‚¬â€ BorderLayout.EAST holds
        // a single child, so the three panels stack inside this box.
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(10, 20, 10));

        // B5-0379: read-only participant list. Refreshed from refresh() on the
        // EDT via refreshParticipantList(); never writes model state Ã¢â‚¬â€ the
        // Support/Oppose buttons stay the sole commit path into the B5-0363
        // offer-collect sequence.
        joinPromptLabel = new JLabel("No active conflict.");
        joinPromptLabel.setForeground(new Color(220, 200, 120));
        joinPromptLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        joinPromptLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        participantListModel = new DefaultListModel();
        participantList = new JList(participantListModel);
        participantList.setEnabled(false);
        participantList.setFocusable(false);
        participantList.setBackground(new Color(10, 15, 30));
        participantList.setForeground(new Color(180, 200, 180));
        participantList.setFont(new Font("Monospaced", Font.PLAIN, 10));
        participantList.setVisibleRowCount(8);
        JScrollPane participantScroll = new JScrollPane(participantList);
        participantScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        participantScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 120, 80)),
            "Conflict Participants", 0, 0, new Font("SansSerif", Font.BOLD, 10),
            new Color(180, 200, 180)));
        participantScroll.getViewport().setBackground(new Color(10, 15, 30));
        JPanel joinPanel = new JPanel();
        joinPanel.setLayout(new BoxLayout(joinPanel, BoxLayout.Y_AXIS));
        joinPanel.setBackground(new Color(10, 20, 10));
        joinPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        joinPromptLabel.setMaximumSize(new Dimension(280, 20));
        participantScroll.setMaximumSize(new Dimension(280, 140));
        participantScroll.setPreferredSize(new Dimension(280, 140));
        joinPanel.add(joinPromptLabel);
        joinPanel.add(participantScroll);
        sidebar.add(joinPanel);
        sidebar.add(logScroll);

        // B5-0329 F9: conflict-type Ã¢â€ â€™ ability legend
        JPanel legendPanel = new JPanel(new GridLayout(4, 2, 4, 2));
        legendPanel.setBackground(new Color(10, 20, 10));
        legendPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 130, 80)),
            "Conflict Types Ã¢â€ â€™ Abilities", 0, 0, new Font("SansSerif", Font.BOLD, 9),
            new Color(180, 200, 180)));
        String[][] legendData = {
            {"DIPLOMACY", "Diplomacy"},
            {"INTRIGUE",  "Intrigue"},
            {"MILITARY",  "Military (Fleets)"},
            {"PSI",       "Psi"}
        };
        for (String[] row : legendData) {
            JLabel typeLabel = new JLabel(row[0]);
            typeLabel.setForeground(new Color(220, 180, 100));
            typeLabel.setFont(new Font("Monospaced", Font.BOLD, 10));
            JLabel abilLabel = new JLabel("Ã¢â€ â€™ " + row[1]);
            abilLabel.setForeground(new Color(180, 200, 180));
            abilLabel.setFont(new Font("Monospaced", Font.PLAIN, 10));
            legendPanel.add(typeLabel);
            legendPanel.add(abilLabel);
        }
        legendPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        legendPanel.setMaximumSize(new Dimension(280, 120));
        sidebar.add(legendPanel);
        add(sidebar, BorderLayout.EAST);

        // Ã¢â€â‚¬Ã¢â€â‚¬ Bottom toolbar Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setBackground(new Color(10, 20, 10));

        statusLabel = new JLabel("InitialisingÃ¢â‚¬Â¦");
        statusLabel.setForeground(new Color(200, 220, 200));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        // B5-0327 F8: initiative order display
        initiativeLabel = new JLabel("Initiative: Ã¢â‚¬â€");
        initiativeLabel.setForeground(new Color(180, 200, 220));
        initiativeLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));

        passButton = makeButton("Pass Turn", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainWindow.this.controller.submitHumanAction(GameAction.pass());
            }
        });

        // B5-0328 F4: split Play/Initiate into two separate buttons, each with
        // its own dispatch so a conflict card can never fire playCard().
        playCardOnlyButton = makeButton("Play Card", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainWindow.this.playOnly();
            }
        });
        playCardOnlyButton.setEnabled(false);

        initiateConflictButton = makeButton("Initiate Conflict", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainWindow.this.initiateOnly();
            }
        });
        initiateConflictButton.setEnabled(false);

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

        discardAgendaButton = makeButton("Discard Agenda", new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                Player hp = humanPlayer();
                if (hp != null && rules.canDiscardAgenda(hp)) {
                    MainWindow.this.controller.submitHumanAction(
                        GameAction.discardAgenda(hp.getAgenda()));
                }
            }
        });
        discardAgendaButton.setEnabled(false);
        discardAgendaButton.setToolTipText("Minor agendas may be discarded; Major agendas must be replaced.");

        replaceAgendaButton = makeButton("Replace Agenda", new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                Player hp = humanPlayer();
                CharacterCard leader = hp == null ? null : findUnrotatedIC(hp);
                if (hp != null && selectedCard instanceof AgendaCard
                        && rules.canReplaceAgenda(hp, (AgendaCard) selectedCard, leader)) {
                    MainWindow.this.controller.submitHumanAction(
                        GameAction.replaceAgenda((AgendaCard) selectedCard, leader));
                    clearSelection();
                }
            }
        });
        replaceAgendaButton.setEnabled(false);
        replaceAgendaButton.setToolTipText("Select an eligible agenda in hand; an Inner Circle character rotates.");

        revealAgendaButton = makeButton("Reveal Agenda", new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                Player hp = humanPlayer();
                if (hp != null && rules.canRevealAgenda(hp)) {
                    MainWindow.this.controller.submitHumanAction(
                        GameAction.revealAgenda(hp.getAgenda()));
                }
            }
        });
        revealAgendaButton.setEnabled(false);
        revealAgendaButton.setToolTipText("Reveal your face-down agenda; it takes effect now.");

        // B5-0381: reveal control for face-down contingencies placed under in-play hosts.
        // The selector lists the human player's unrevealed contingencies via B5-0394
        // accessors (GameState.getPlacedContingencies); the Reveal button submits
        // REVEAL_CONTINGENCY for the selected one through the existing human pipeline.
        String[] empty = new String[] { "(none)" };
        contingencySelector = new JComboBox<String>(empty);
        contingencySelector.setEnabled(false);
        contingencySelector.setMaximumSize(new Dimension(180, 24));

        revealContingencyButton = makeButton("Reveal Contingency", new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                Player hp = humanPlayer();
                if (hp == null) return;
                GameState gs = MainWindow.this.controller.getState();
                java.util.List<ContingencyCard> placed = gs.getPlacedContingencies(hp);
                if (placed.isEmpty()) return;
                int sel = contingencySelector.getSelectedIndex();
                if (sel < 0 || sel >= placed.size()) return;
                ContingencyCard cc = placed.get(sel);
                if (rules.canRevealContingency(hp, cc)) {
                    MainWindow.this.controller.submitHumanAction(
                        GameAction.revealContingency(cc));
                }
            }
        });
        revealContingencyButton.setEnabled(false);
        revealContingencyButton.setToolTipText("Reveal a face-down contingency placed under one of your in-play hosts.");

        // B5-0401: Lead Fleet control — select a ready character (IC or supporting)
        // and an unrotated fleet to rotate the character and lead the fleet.
        leadFleetSelector = new JComboBox<String>(new String[] { "(select fleet)" });
        leadFleetSelector.setEnabled(false);
        leadFleetSelector.setMaximumSize(new Dimension(180, 24));
        leadFleetSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (leadFleetSelector.getSelectedItem() != null) {
                    String name = (String) leadFleetSelector.getSelectedItem();
                    Player hp = humanPlayer();
                    if (hp != null) {
                        for (FleetCard fl : hp.getFleets()) {
                            if (fl.getTitle().equals(name)) {
                                selectedFleet = fl;
                                break;
                            }
                        }
                    }
                    GameState gs = MainWindow.this.controller.getState();
                    boolean actionTurn = gs.getPhase() == GamePhase.ACTION
                        && gs.getActivePlayer() == hp && MainWindow.this.controller.isWaitingForHuman();
                    updateLeadFleetButton(actionTurn);
                }
            }
        });

        leadFleetButton = makeButton("Lead Fleet", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player hp = humanPlayer();
                CharacterCard leader = (selectedCard instanceof CharacterCard)
                    ? (CharacterCard) selectedCard : null;
                if (hp != null && leader != null && selectedFleet != null
                        && rules.canLeadFleet(hp, leader, selectedFleet)) {
                    MainWindow.this.controller.submitHumanAction(
                        GameAction.leadFleet(leader, selectedFleet));
                    clearSelection();
                }
            }
        });
        leadFleetButton.setEnabled(false);
        leadFleetButton.setToolTipText("Rotate a ready IC/supporting character to lead an unrotated fleet (B5-0362).");

        // B5-0401: Use Rotate Effect control — select an assistant and effect kind.
        rotateEffectKindSelector = new JComboBox<String>(
            new String[] { "Ability Boost (+1 D/I/L)", "Sponsor Discount (-1 INF)" });
        rotateEffectKindSelector.setEnabled(false);
        rotateEffectKindSelector.setMaximumSize(new Dimension(180, 24));

        useRotateEffectButton = makeButton("Use Rotate Effect", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player hp = humanPlayer();
                CharacterCard assistant = (selectedAssistant instanceof CharacterCard)
                    ? (CharacterCard) selectedAssistant : null;
                if (hp != null && assistant != null) {
                    GameAction.RotateEffectKind kind = rotateEffectKindSelector.getSelectedIndex() == 0
                        ? GameAction.RotateEffectKind.USE_ABILITY_BOOST
                        : GameAction.RotateEffectKind.USE_SPONSOR_DISCOUNT;
                    CharacterCard amb = hp.getAmbassador();
                    if (rules.canUseRotateEffect(hp, assistant, amb, kind)) {
                        MainWindow.this.controller.submitHumanAction(
                            GameAction.useRotateEffect(assistant, amb, kind));
                        clearSelection();
                    }
                }
            }
        });
        useRotateEffectButton.setEnabled(false);
        useRotateEffectButton.setToolTipText("Rotate a ready supporting assistant to boost ambassador or reduce sponsor cost (B5-0366).");

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
                    updatePlayInitiateButtons();
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

        // B5-0402: Tier-3 action UI — Attack + Heal + Repair
        attackButton = makeButton("Attack", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player hp = humanPlayer();
                Conflict conflict = MainWindow.this.controller.getState().getActiveConflict();
                if (hp == null || conflict == null) return;
                if (!(selectedCard instanceof Card)) return;
                Card attacker = (Card) selectedCard;
                // Find a valid target from participants on another faction's side
                Card validTarget = null;
                for (Player p : conflict.getParticipants()) {
                    if (p == hp) continue; // can't attack ourselves
                    for (Card c : conflict.getCommittedCards(p)) {
                        if (rules.canAttackConflictParticipant(hp, attacker, c, conflict)) {
                            validTarget = c;
                            break;
                        }
                    }
                    if (validTarget != null) break;
                }
                if (validTarget != null) {
                    MainWindow.this.controller.submitHumanAction(
                        GameAction.attackConflictParticipant(attacker, validTarget));
                    clearSelection();
                }
            }
        });
        attackButton.setEnabled(false);
        attackButton.setToolTipText("Attack a participant in the active conflict (B5-0370).");

        healButton = makeButton("Heal", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player hp = humanPlayer();
                if (hp != null && selectedCard instanceof CharacterCard) {
                    CharacterCard ch = (CharacterCard) selectedCard;
                    if (rules.canHealCharacter(hp, ch)) {
                        MainWindow.this.controller.submitHumanAction(
                            GameAction.healCharacter(ch));
                        clearSelection();
                    }
                }
            }
        });
        healButton.setEnabled(false);
        healButton.setToolTipText("Rotate to heal a damaged IC/supporting character (B5-0371).");

        repairButton = makeButton("Repair", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player hp = humanPlayer();
                if (hp != null && selectedCard instanceof FleetCard) {
                    FleetCard fl = (FleetCard) selectedCard;
                    if (rules.canRepairCard(hp, fl)) {
                        MainWindow.this.controller.submitHumanAction(
                            GameAction.repairCard(fl));
                        clearSelection();
                    }
                } else if (hp != null && selectedCard instanceof LocationCard) {
                    LocationCard loc = (LocationCard) selectedCard;
                    if (rules.canRepairCard(hp, loc)) {
                        MainWindow.this.controller.submitHumanAction(
                            GameAction.repairCard(loc));
                        clearSelection();
                    }
                }
            }
        });
        repairButton.setEnabled(false);
        repairButton.setToolTipText("Pool-paid repair of a damaged fleet or location (B5-0371).");

        // B5-0404: mercenary bid control
        mercenaryBidAmountSelector = new JComboBox<String>(BID_AMOUNTS);
        mercenaryBidAmountSelector.setEnabled(false);
        mercenaryBidAmountSelector.setMaximumSize(new Dimension(80, 24));
        mercenaryBidButton = makeButton("Bid", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player hp = humanPlayer();
                if (hp == null) return;
                GameState gs = MainWindow.this.controller.getState();
                List<Card> offers = gs.getMercenaryOffers();
                if (offers.isEmpty()) return;
                int amount = Integer.parseInt((String) mercenaryBidAmountSelector.getSelectedItem());
                if (amount <= 0) return;
                // Bid on the first offered mercenary (single-mercenary display assumption)
                Card merc = offers.get(0);
                if (rules.canBidOnMercenary(hp, merc, amount, gs)) {
                    MainWindow.this.controller.submitHumanAction(
                        GameAction.bidOnMercenary(merc, amount));
                }
            }
        });
        mercenaryBidButton.setEnabled(false);
        mercenaryBidButton.setToolTipText("Bid influence to control an offered mercenary (B5-0395).");

        mercenaryOfferLabel = new JLabel("(no mercenary offers)");
        mercenaryOfferLabel.setForeground(new Color(200, 220, 200));
        mercenaryOfferLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        mercenaryOfferLabel.setMaximumSize(new Dimension(160, 24));

        // B5-0407: declare-war UI
        warTargetSelector = new JComboBox<String>(new String[] { "(select target)" });
        warTargetSelector.setEnabled(false);
        warTargetSelector.setMaximumSize(new Dimension(200, 24));
        warTargetSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateDeclareWarButton();
            }
        });

        declareWarButton = makeButton("Declare War", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player hp = humanPlayer();
                if (hp == null) return;
                GameState gs = MainWindow.this.controller.getState();
                String sel = (String) warTargetSelector.getSelectedItem();
                if (sel == null || sel.equals("(select target)")
                        || sel.equals("(no legal targets)")) return;
                // Determine if this is a race target or location target
                Player raceTarget = null;
                LocationCard locTarget = null;
                // Look up the target by name
                for (Player p : gs.getPlayers()) {
                    if (!p.isHuman() && p.getName().equals(sel)) {
                        raceTarget = p;
                        break;
                    }
                }
                if (raceTarget == null) {
                    // Must be a location — find by scanning all players' locations
                    for (Player p : gs.getPlayers()) {
                        if (p.isHuman()) continue;
                        for (LocationCard loc : p.getLocations()) {
                            if (loc.getTitle().equals(sel)) {
                                locTarget = loc;
                                break;
                            }
                        }
                        if (locTarget != null) break;
                    }
                }
                if (raceTarget == null && locTarget == null) return;
                WarKind kind = raceTarget != null ? WarKind.RACE_TARGET : WarKind.LOCATION_TARGET;
                if (rules.canInitiateWarConflict(hp, kind, raceTarget, locTarget, gs)) {
                    MainWindow.this.controller.submitHumanAction(
                            GameAction.declareWarConflict(kind, raceTarget, locTarget));
                }
            }
        });
        declareWarButton.setEnabled(false);
        declareWarButton.setToolTipText("Declare a war conflict against a race or location at war with your faction (B5-0376).");

        warStatusLabel = new JLabel("");
        warStatusLabel.setForeground(new Color(200, 220, 200));
        warStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        warStatusLabel.setMaximumSize(new Dimension(240, 24));

        // B5-0326 F5: cost preview readout
        costLabel = new JLabel("  ");
        costLabel.setForeground(new Color(200, 220, 200));
        costLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));

        // B5-0348: hand filter/sort panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        filterPanel.setBackground(new Color(10, 20, 10));
        filterPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 120, 80)),
            "Filter / Sort", 0, 0, new Font("SansSerif", Font.BOLD, 9),
            new Color(180, 200, 180)));

        // Type filters
        typeCharBox = makeTinyCheckBox("Char");
        typeFleetBox = makeTinyCheckBox("Fleet");
        typeConflictBox = makeTinyCheckBox("Conflict");
        typeAgendaBox = makeTinyCheckBox("Agenda");
        typeAftermathBox = makeTinyCheckBox("Aftermath");
        typeEventBox = makeTinyCheckBox("Event");
        typeEnhancementBox = makeTinyCheckBox("Enh");
        typeGroupBox = makeTinyCheckBox("Group");
        typeLocationBox = makeTinyCheckBox("Loc");
        typeCharBox.setSelected(true);
        typeFleetBox.setSelected(true);
        typeConflictBox.setSelected(true);
        typeAgendaBox.setSelected(true);
        typeAftermathBox.setSelected(true);
        typeEventBox.setSelected(true);
        typeEnhancementBox.setSelected(true);
        typeGroupBox.setSelected(true);
        typeLocationBox.setSelected(true);

        // Faction filters
        fascHumanBox = makeTinyCheckBox("Human");
        fascMinbariBox = makeTinyCheckBox("Minbari");
        fascCentauriBox = makeTinyCheckBox("Centauri");
        fascNarnBox = makeTinyCheckBox("Narn");
        fascNeutralBox = makeTinyCheckBox("Neut");
        fascNonAlignedBox = makeTinyCheckBox("NonAl");
        fascVorlonBox = makeTinyCheckBox("Vorlon");
        fascAnyBox = makeTinyCheckBox("ANY");
        fascHumanBox.setSelected(true);
        fascMinbariBox.setSelected(true);
        fascCentauriBox.setSelected(true);
        fascNarnBox.setSelected(true);
        fascNeutralBox.setSelected(true);
        fascNonAlignedBox.setSelected(true);
        fascVorlonBox.setSelected(true);
        fascAnyBox.setSelected(true);

        // Sort controls
        sortUnsorted = new JRadioButton("Order");
        sortCostAsc = new JRadioButton("CostÃ¢â€ â€˜");
        sortCostDesc = new JRadioButton("CostÃ¢â€ â€œ");
        sortUnsorted.setSelected(true);
        sortUnsorted.setFocusPainted(false);
        sortCostAsc.setFocusPainted(false);
        sortCostDesc.setFocusPainted(false);
        sortGroup = new ButtonGroup();
        sortGroup.add(sortUnsorted);
        sortGroup.add(sortCostAsc);
        sortGroup.add(sortCostDesc);
        sortUnsorted.setFont(new Font("SansSerif", Font.PLAIN, 9));
        sortCostAsc.setFont(new Font("SansSerif", Font.PLAIN, 9));
        sortCostDesc.setFont(new Font("SansSerif", Font.PLAIN, 9));

        // Show unplayable toggle
        showUnplayable = makeTinyCheckBox("Show dimmed");
        showUnplayable.setSelected(true);

        filterPanel.add(new JSeparator(JSeparator.VERTICAL));
        filterPanel.add(new JLabel("Type:"));
        filterPanel.add(typeCharBox);
        filterPanel.add(typeFleetBox);
        filterPanel.add(typeConflictBox);
        filterPanel.add(typeAgendaBox);
        filterPanel.add(typeAftermathBox);
        filterPanel.add(typeEventBox);
        filterPanel.add(typeEnhancementBox);
        filterPanel.add(typeGroupBox);
        filterPanel.add(typeLocationBox);
        filterPanel.add(new JSeparator(JSeparator.VERTICAL));
        filterPanel.add(new JLabel("Faction:"));
        filterPanel.add(fascHumanBox);
        filterPanel.add(fascMinbariBox);
        filterPanel.add(fascCentauriBox);
        filterPanel.add(fascNarnBox);
        filterPanel.add(fascNeutralBox);
        filterPanel.add(fascNonAlignedBox);
        filterPanel.add(fascVorlonBox);
        filterPanel.add(fascAnyBox);
        filterPanel.add(new JSeparator(JSeparator.VERTICAL));
        filterPanel.add(sortUnsorted);
        filterPanel.add(sortCostAsc);
        filterPanel.add(sortCostDesc);
        filterPanel.add(new JSeparator(JSeparator.VERTICAL));
        filterPanel.add(showUnplayable);

        // B5-0348: hand filter/sort panel
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(filterPanel);
        toolbar.add(Box.createHorizontalStrut(8));

        // Assemble toolbar
        toolbar.add(passButton);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(playCardOnlyButton);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(initiateConflictButton);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(sponsorButton);
        toolbar.add(promoteButton);
        toolbar.add(buildInfluenceButton);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(discardAgendaButton);
        toolbar.add(replaceAgendaButton);
        toolbar.add(revealAgendaButton);
        toolbar.add(contingencySelector);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(revealContingencyButton);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(leadFleetSelector);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(leadFleetButton);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(rotateEffectKindSelector);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(useRotateEffectButton);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(costLabel);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(targetSelector);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(supportButton);
        toolbar.add(opposeButton);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(attackButton);
        toolbar.add(healButton);
        toolbar.add(repairButton);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(mercenaryBidAmountSelector);
        toolbar.add(mercenaryBidButton);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(mercenaryOfferLabel);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(warTargetSelector);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(declareWarButton);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(warStatusLabel);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(initiativeLabel);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(statusLabel);

        add(toolbar, BorderLayout.NORTH);

        // Ã¢â€â‚¬Ã¢â€â‚¬ Hand Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
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
                // B5-0401: Track assistant selection for Use Rotate Effect
                Player hp = humanPlayer();
                if (card instanceof CharacterCard) {
                    CharacterCard ch = (CharacterCard) card;
                    if (hp != null && hp.getSupportingRole().contains(ch)) {
                        selectedAssistant = ch;
                    } else {
                        selectedAssistant = null;
                    }
                } else {
                    selectedAssistant = null;
                }
                updatePlayInitiateButtons();
                // B5-0326 F5: refresh cost preview immediately
                refreshCostPreview();
                // B5-0401: Refresh Tier-1 remainder action UI
                GameState gs = MainWindow.this.controller.getState();
                refreshLeadFleetAndRotateEffect(hp, gs.getPhase() == GamePhase.ACTION
                    && gs.getActivePlayer() == hp && MainWindow.this.controller.isWaitingForHuman());
            }
        });
        // B5-0348: give HandPanel access to rules + human faction for affordability checks
        handPanel.setRules(rules, controller.getState().getHumanPlayer().getFaction());
        add(handPanel, BorderLayout.SOUTH);

        // B5-0348: wire filter/sort controls
        typeCharBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setTypeFilter(CardType.CHARACTER, typeCharBox.isSelected()); }
        });
        typeFleetBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setTypeFilter(CardType.FLEET, typeFleetBox.isSelected()); }
        });
        typeConflictBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setTypeFilter(CardType.CONFLICT, typeConflictBox.isSelected()); }
        });
        typeAgendaBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setTypeFilter(CardType.AGENDA, typeAgendaBox.isSelected()); }
        });
        typeAftermathBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setTypeFilter(CardType.AFTERMATH, typeAftermathBox.isSelected()); }
        });
        typeEventBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setTypeFilter(CardType.EVENT, typeEventBox.isSelected()); }
        });
        typeEnhancementBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setTypeFilter(CardType.ENHANCEMENT, typeEnhancementBox.isSelected()); }
        });
        typeGroupBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setTypeFilter(CardType.GROUP, typeGroupBox.isSelected()); }
        });
        typeLocationBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setTypeFilter(CardType.LOCATION, typeLocationBox.isSelected()); }
        });
        fascHumanBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setFactionFilter(Faction.HUMAN, fascHumanBox.isSelected()); }
        });
        fascMinbariBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setFactionFilter(Faction.MINBARI, fascMinbariBox.isSelected()); }
        });
        fascCentauriBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setFactionFilter(Faction.CENTAURI, fascCentauriBox.isSelected()); }
        });
        fascNarnBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setFactionFilter(Faction.NARN, fascNarnBox.isSelected()); }
        });
        fascNeutralBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setFactionFilter(Faction.NEUTRAL, fascNeutralBox.isSelected()); }
        });
        fascNonAlignedBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setFactionFilter(Faction.NON_ALIGNED, fascNonAlignedBox.isSelected()); }
        });
        fascVorlonBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setFactionFilter(Faction.VORLON, fascVorlonBox.isSelected()); }
        });
        fascAnyBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setFactionFilter(Faction.ANY, fascAnyBox.isSelected()); }
        });
        sortUnsorted.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setSortMode(0); }
        });
        sortCostAsc.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setSortMode(1); }
        });
        sortCostDesc.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setSortMode(2); }
        });
        showUnplayable.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { handPanel.setShowUnplayable(showUnplayable.isSelected()); }
        });

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

    /** B5-0404: refresh the mercenary bid control from current state.
     *  Shows offered mercenaries, updates the amount selector enablement and
     *  the Bid button. The button is enabled when it is the human's Action
     *  turn, a mercenary is offered, and the selected bid amount is affordable.
     */
    private void refreshMercenaryBidControl(Player human, boolean actionTurn) {
        if (human == null) {
            mercenaryBidAmountSelector.setEnabled(false);
            mercenaryBidButton.setEnabled(false);
            mercenaryOfferLabel.setText("(no mercenary offers)");
            return;
        }
        GameState gs = MainWindow.this.controller.getState();
        List<Card> offers = gs.getMercenaryOffers();
        if (offers.isEmpty()) {
            mercenaryBidAmountSelector.setEnabled(false);
            mercenaryBidButton.setEnabled(false);
            mercenaryOfferLabel.setText("(no mercenary offers)");
            return;
        }
        // Show the first offered mercenary's title
        mercenaryOfferLabel.setText(offers.get(0).getTitle());

        // Enablement: Action phase, my turn, a mercenary is offered
        mercenaryBidAmountSelector.setEnabled(actionTurn && !offers.isEmpty());

        // Bid button: enabled when affordable at the selected amount
        int selectedAmount = Integer.parseInt((String) mercenaryBidAmountSelector.getSelectedItem());
        boolean canAfford = actionTurn
            && rules.canBidOnMercenary(human, offers.get(0), selectedAmount, gs);
        mercenaryBidButton.setEnabled(canAfford);
    }

    /** B5-0407: refresh the declare-war control from current state.
     *  Populates the target selector with legal war targets (races at war,
     *  then locations owned by factions at war), updates the status label,
     *  and enables/disables the controls based on phase and turn.
     */
    private void refreshDeclareWarControl(Player human, boolean actionTurn) {
        if (human == null) {
            warTargetSelector.setEnabled(false);
            declareWarButton.setEnabled(false);
            warStatusLabel.setText("");
            return;
        }
        GameState gs = MainWindow.this.controller.getState();
        // Only available when at war
        if (!gs.isAtWar(human.getFaction())) {
            warTargetSelector.setEnabled(false);
            declareWarButton.setEnabled(false);
            warStatusLabel.setText("Not at war — no war conflicts available.");
            return;
        }
        // Build target list: races at war first, then locations
        java.util.ArrayList<String> targets = new java.util.ArrayList<String>();
        java.util.ArrayList<String> targetTypes = new java.util.ArrayList<String>();
        for (Player p : gs.getPlayers()) {
            if (p == human || p.getFaction() == null) continue;
            if (gs.isAtWar(human.getFaction(), p.getFaction())) {
                targets.add(p.getName());
                targetTypes.add("race");
            }
        }
        for (Player p : gs.getPlayers()) {
            if (p == human || p.getFaction() == null) continue;
            if (!gs.isAtWar(human.getFaction(), p.getFaction())) continue;
            for (LocationCard loc : p.getLocations()) {
                targets.add(loc.getTitle());
                targetTypes.add("location");
            }
        }
        if (targets.isEmpty()) {
            warTargetSelector.setEnabled(false);
            declareWarButton.setEnabled(false);
            warStatusLabel.setText("No war targets available.");
            return;
        }
        warTargetSelector.removeAllItems();
        for (int i = 0; i < targets.size(); i++) {
            String label = targets.get(i);
            if (targetTypes.get(i).equals("location")) {
                label += " [loc]";
            }
            warTargetSelector.addItem(label);
        }
        warTargetSelector.setEnabled(actionTurn);
        warStatusLabel.setText(targets.size() + " war target(s) available.");
        updateDeclareWarButton();
    }

    /** Updates the Declare War button enablement based on current selection. */
    private void updateDeclareWarButton() {
        if (warTargetSelector == null || declareWarButton == null) return;
        Player hp = humanPlayer();
        if (hp == null) { declareWarButton.setEnabled(false); return; }
        GameState gs = MainWindow.this.controller.getState();
        String sel = (String) warTargetSelector.getSelectedItem();
        if (sel == null || sel.equals("(select target)")
                || sel.equals("(no legal targets)")) {
            declareWarButton.setEnabled(false);
            return;
        }
        // Determine target type
        Player raceTarget = null;
        LocationCard locTarget = null;
        for (Player p : gs.getPlayers()) {
            if (!p.isHuman() && p.getName().equals(sel)) {
                raceTarget = p;
                break;
            }
        }
        if (raceTarget == null) {
            for (Player p : gs.getPlayers()) {
                if (p.isHuman()) continue;
                for (LocationCard loc : p.getLocations()) {
                    if (loc.getTitle().equals(sel)) {
                        locTarget = loc;
                        break;
                    }
                }
                if (locTarget != null) break;
            }
        }
        if (raceTarget == null && locTarget == null) {
            declareWarButton.setEnabled(false);
            return;
        }
        WarKind kind = raceTarget != null ? WarKind.RACE_TARGET : WarKind.LOCATION_TARGET;
        boolean actionTurn = gs.getPhase() == GamePhase.ACTION
            && gs.getActivePlayer() == hp
            && MainWindow.this.controller.isWaitingForHuman();
        declareWarButton.setEnabled(actionTurn
            && rules.canInitiateWarConflict(hp, kind, raceTarget, locTarget, gs));
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
        // Build Influence Ã¢â‚¬â€ not card-specific, but show if available
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
        // B5-0361: broker the UI-held conflict + state into the hand panel so
        // the eligible-aftermath highlight consults the real 6-arg legality
        // (D2 targets + D4 registry) instead of guessing.
        handPanel.setResolvedConflictContext(boardPanel.getLastHeldConflict(), state);
        handPanel.update(human.getHand(), rules, human);

        // Append new log lines
        java.util.List<String> log = state.getLog();
        logArea.setText("");
        String currentPhasePrefix = null;
        int currentRound = -1;
        for (String line : log) {
            // B5-0331 F12: detect round/phase grouping from log prefixes
            String prefix = "";
            String content = line;
            int colonIdx = line.indexOf(':');
            if (colonIdx > 0 && colonIdx < 20) {
                String candidate = line.substring(0, colonIdx).trim();
                // B5-0331a: escape-immune prefix tests. The regex literals
                // previously here ended up double-escaped (matching a literal
                // backslash) so round/phase grouping could never fire.
                if (MainWindow.isRoundPrefix(candidate)
                    || MainWindow.isPhasePrefix(candidate)) {
                    prefix = candidate + ":";
                    content = line.substring(colonIdx + 1).trim();
                }
            }
            // Insert phase/group headers when they change
            if (!prefix.isEmpty()) {
                if (!prefix.startsWith("Round")) {
                    // Phase line Ã¢â‚¬â€ insert phase header before this line
                    if (currentPhasePrefix == null || !currentPhasePrefix.equals(prefix)) {
                        logArea.append("\nÃ¢â€â‚¬Ã¢â€â‚¬ " + prefix + " Ã¢â€â‚¬Ã¢â€â‚¬\n");
                        currentPhasePrefix = prefix;
                    }
                } else {
                    // Round line Ã¢â‚¬â€ insert round header before this line
                    if (currentRound == -1 || !prefix.equals("Round " + currentRound)) {
                        logArea.append("\nÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢Â\n");
                        logArea.append(prefix + "\n");
                        logArea.append("Ã¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢ÂÃ¢â€¢Â\n");
                        try {
                            currentRound = Integer.parseInt(prefix.substring(6).trim());
                        } catch (NumberFormatException e) {
                            currentRound = -1;
                        }
                    }
                }
            }
            logArea.append(content + "\n");
        }
        logArea.setCaretPosition(logArea.getDocument().getLength());

        boolean myTurn = state.getActivePlayer() == human
            && MainWindow.this.controller.isWaitingForHuman();
        boolean activeConflict = state.getActiveConflict() != null;
        GamePhase phase = state.getPhase();

        // B5-0363: join controls are enabled only while the controller is
        // collecting this human seat's side for an active, legal conflict.
        boolean joiningConflict = activeConflict
            && MainWindow.this.controller.isWaitingForHumanConflictJoin()
            && rules.canJoinConflict(human, state.getActiveConflict())
            && state.getActiveConflict().canJoinConflict(human);
        supportButton.setEnabled(joiningConflict);
        opposeButton.setEnabled(joiningConflict);
        refreshParticipantList(state, human, joiningConflict);

        passButton.setEnabled(myTurn);

        // B5-0328 F4: split Play/Initiate Ã¢â‚¬â€ enablement lives in one authority,
        // updatePlayInitiateButtons(); dispatch in playOnly() / initiateOnly().
        updatePlayInitiateButtons();

        // B5-0327 F8: initiative order display Ã¢â‚¬â€ show active player as the current
        // initiative holder. The human player's initiative position is tracked in the
        // status bar for clarity; the full initiative chain is rendered on the board.
        initiativeLabel.setText("Initiative: " + state.getActivePlayer().getName()
            + (myTurn ? " (you)" : ""));
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

        // B5-0402: Attack - enabled when ACTION phase, my turn, active conflict, selected card can attack
        Card selCard = selectedCard;
        boolean canAttack = actionPhase && myTurn && activeConflict && selCard instanceof Card
            && !selCard.isFaceDown() && !selCard.isRotated()
            && selCard.canActAfterNeutralization();
        attackButton.setEnabled(canAttack);

        // Heal: ACTION phase, my turn, selected character in IC/supporting, damaged
        boolean canHeal = actionPhase && myTurn && ch != null && !ch.isFaceDown()
            && (human.getInnerCircle().contains(ch) || human.getSupportingRole().contains(ch))
            && ch.getDamageTokens() > 0;
        healButton.setEnabled(canHeal);

        // Repair: ACTION phase, my turn, selected fleet/location with damage, affordable
        boolean canRepair = false;
        if (actionPhase && myTurn && selCard instanceof FleetCard) {
            FleetCard fl = (FleetCard) selCard;
            canRepair = !fl.isFaceDown() && !fl.isRotated() && fl.getDamageTokens() > 0
                && rules.canRepairCard(human, fl);
        } else if (actionPhase && myTurn && selCard instanceof LocationCard) {
            LocationCard loc = (LocationCard) selCard;
            canRepair = !loc.isFaceDown() && !loc.isRotated() && loc.getDamageTokens() > 0
                && rules.canRepairCard(human, loc);
        }
        repairButton.setEnabled(canRepair);

        // B5-0380: lifecycle controls are action-phase-only. The disabled
        // discard control remains visible for a Major agenda and its tooltip
        // explains why replacement is the available path.
        refreshAgendaControls(human, actionPhase && myTurn);

        // B5-0381: contingency reveal control — enabled when the human has
        // unrevealed contingencies placed under in-play hosts and it is
        // the human's Action turn.
        refreshContingencyRevealControl(human, actionPhase && myTurn);

        // B5-0401: Tier-1 remainder action UI — Lead Fleet + Use Rotate Effect
        refreshLeadFleetAndRotateEffect(human, actionPhase && myTurn);

        // B5-0404: mercenary bid control
        refreshMercenaryBidControl(human, actionPhase && myTurn);

        // B5-0407: declare-war control
        refreshDeclareWarControl(human, actionPhase && myTurn);

        // B5-0326 F5: refresh cost preview
        refreshCostPreview();

        if (state.isGameOver()) {
            statusLabel.setText("GAME OVER Ã¢â‚¬â€ Winner: "
                + (state.getWinner() != null ? state.getWinner().getName() : "None"));
            passButton.setEnabled(false);
            playCardOnlyButton.setEnabled(false);
            initiateConflictButton.setEnabled(false);
        } else {
            if (joiningConflict) {
                statusLabel.setText("Round " + state.getRoundNumber()
                    + "  |  Conflict: choose Support or Oppose");
            } else {
                statusLabel.setText("Round " + state.getRoundNumber()
                    + "  |  " + state.getPhase()
                    + "  |  Active: " + state.getActivePlayer().getName()
                    + (myTurn ? "  Ã¢â€ Â YOUR TURN" : ""));
            }
        }
    }

    /** B5-0380: refresh the agenda lifecycle affordances from current state. */
    private void refreshAgendaControls(Player human, boolean actionTurn) {
        if (human == null) {
            discardAgendaButton.setEnabled(false);
            replaceAgendaButton.setEnabled(false);
            revealAgendaButton.setEnabled(false);
            revealContingencyButton.setEnabled(false);
            return;
        }
        AgendaCard current = human.getAgenda();
        boolean major = current != null && current.isMajorAgenda();
        discardAgendaButton.setText(major ? "Discard Agenda (Major)" : "Discard Agenda");
        discardAgendaButton.setToolTipText(major
            ? "Major agendas cannot be discarded; select another Major agenda and use Replace Agenda."
            : "Discard your current minor agenda.");
        discardAgendaButton.setEnabled(actionTurn && rules.canDiscardAgenda(human));

        CharacterCard leader = findUnrotatedIC(human);
        boolean validReplacement = selectedCard instanceof AgendaCard
            && rules.canReplaceAgenda(human, (AgendaCard) selectedCard, leader);
        replaceAgendaButton.setEnabled(actionTurn && validReplacement);
        replaceAgendaButton.setToolTipText(major
            ? "Select a Major agenda in hand; an Inner Circle character rotates and the current Major is removed from the game."
            : "Select an eligible agenda in hand; an Inner Circle character rotates and the current agenda is removed from the game.");

        revealAgendaButton.setEnabled(actionTurn && rules.canRevealAgenda(human));
        revealAgendaButton.setToolTipText("Reveal your face-down agenda; it takes effect now.");
    }

    /** B5-0381: refresh the contingency reveal control from current state. */
    private void refreshContingencyRevealControl(Player human, boolean actionTurn) {
        if (human == null || contingencySelector == null) {
            if (contingencySelector != null) {
                contingencySelector.removeAllItems();
                contingencySelector.addItem("(none)");
                contingencySelector.setEnabled(false);
            }
            revealContingencyButton.setEnabled(false);
            return;
        }
        GameState gs = MainWindow.this.controller.getState();
        java.util.List<ContingencyCard> placed = gs.getPlacedContingencies(human);
        boolean hasUnrevealed = false;
        contingencySelector.removeAllItems();
        for (int i = 0; i < placed.size(); i++) {
            ContingencyCard cc = placed.get(i);
            if (!cc.isRevealed() && rules.canRevealContingency(human, cc)) {
                hasUnrevealed = true;
            }
            String label = cc.getTitle();
            Card host = cc.getPlacedUnder();
            if (host != null) label += " (under " + host.getTitle() + ")";
            contingencySelector.addItem(label);
        }
        if (placed.isEmpty()) {
            contingencySelector.addItem("(none)");
            revealContingencyButton.setEnabled(false);
            revealContingencyButton.setToolTipText("No contingencies placed.");
        } else {
            contingencySelector.setEnabled(true);
            revealContingencyButton.setEnabled(actionTurn && hasUnrevealed);
            int unrevealed = 0;
            for (ContingencyCard cc : placed) {
                if (!cc.isRevealed()) unrevealed++;
            }
            revealContingencyButton.setToolTipText("Reveal a face-down contingency (" + unrevealed + " remaining).");
        }
}
 
    /** B5-0401: refresh Lead Fleet and Use Rotate Effect controls. */
    private void refreshLeadFleetAndRotateEffect(Player human, boolean actionTurn) {
        if (human == null || leadFleetSelector == null || rotateEffectKindSelector == null) {
            if (leadFleetSelector != null) {
                leadFleetSelector.removeAllItems();
                leadFleetSelector.addItem("(select fleet)");
                leadFleetSelector.setEnabled(false);
            }
            if (rotateEffectKindSelector != null) {
                rotateEffectKindSelector.setEnabled(false);
            }
            leadFleetButton.setEnabled(false);
            useRotateEffectButton.setEnabled(false);
            return;
        }
 
        // Lead Fleet: populate fleet selector if a valid leader character is selected
        CharacterCard leader = (selectedCard instanceof CharacterCard)
            ? (CharacterCard) selectedCard : null;
        boolean hasValidLeader = leader != null
            && !leader.isFaceDown() && !leader.isRotated()
            && (human.getInnerCircle().contains(leader) || human.getSupportingRole().contains(leader))
            && leader.canActAfterNeutralization();
 
        leadFleetSelector.removeAllItems();
        int fleetCount = 0;
        if (hasValidLeader) {
            for (FleetCard fl : human.getFleets()) {
                if (!fl.isRotated() && fl.canActAfterNeutralization() && fl.getLeader() == null) {
                    leadFleetSelector.addItem(fl.getTitle());
                    fleetCount++;
                }
            }
        }
        if (fleetCount == 0) {
            leadFleetSelector.addItem("(no eligible fleet)");
        }
        leadFleetSelector.setEnabled(fleetCount > 0);
        updateLeadFleetButton(actionTurn);
 
        // Use Rotate Effect: enabled when a supporting assistant is selected and ambassador exists
        CharacterCard assistant = (selectedAssistant instanceof CharacterCard)
            ? (CharacterCard) selectedAssistant : null;
        boolean hasValidAssistant = assistant != null
            && !assistant.isFaceDown() && !assistant.isRotated()
            && human.getSupportingRole().contains(assistant)
            && assistant.canActAfterNeutralization()
            && human.getAmbassador() != null;
 
        rotateEffectKindSelector.setEnabled(hasValidAssistant);
        useRotateEffectButton.setEnabled(actionTurn && hasValidAssistant);
    }
 
    /** Updates the Lead Fleet button enablement based on current fleet selection. */
    private void updateLeadFleetButton(boolean actionTurn) {
        if (leadFleetSelector == null || leadFleetButton == null) return;
        String sel = (String) leadFleetSelector.getSelectedItem();
        boolean hasFleet = sel != null && !sel.equals("(select fleet)") && !sel.equals("(no eligible fleet)");
        Player hp = humanPlayer();
        CharacterCard leader = (selectedCard instanceof CharacterCard)
            ? (CharacterCard) selectedCard : null;
        leadFleetButton.setEnabled(actionTurn && hasFleet && leader != null && hp != null
            && rules.canLeadFleet(hp, leader, selectedFleet));
    }
 
    /**
     * B5-0379: rebuild the read-only join-window participant list from the
     * live Conflict D14 sides API (B5-0309). Runs on the EDT inside
     * refresh(); reads only, so it never mutates model or engine state.
     * Committed lists are snapshotted before iteration so a commit landing
     * mid-refresh cannot throw ConcurrentModificationException.
     */
    private void refreshParticipantList(GameState state, Player human, boolean joiningConflict) {
        if (participantListModel == null || joinPromptLabel == null) return;
        participantListModel.removeAllElements();
        Conflict c = state.getActiveConflict();
        if (c == null) {
            joinPromptLabel.setText("No active conflict.");
            return;
        }
        StringBuffer title = new StringBuffer();
        title.append(c.getCard().getTitle());
        title.append(" [").append(c.getConflictType()).append("]");
        if (c.getInitiator() != null) {
            title.append(" by ").append(c.getInitiator().getName());
        }
        if (c.getTarget() != null) {
            title.append(" targets ").append(c.getTarget().getName());
        }
        title.append(" : support ").append(c.supportTotal());
        title.append(" vs oppose ").append(c.oppositionTotal());
        joinPromptLabel.setText(title.toString());
        java.util.List players = state.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player pl = (Player) players.get(i);
            boolean support = c.isSupporting(pl);
            boolean oppose = c.isOpposing(pl);
            if (!support && !oppose) continue;
            java.util.ArrayList cards = new java.util.ArrayList(c.getCommittedCards(pl));
            StringBuffer line = new StringBuffer();
            if (support) {
                line.append("[support] ");
            } else {
                line.append("[oppose]  ");
            }
            line.append(pl.getName());
            if (pl == human) line.append(" (you)");
            line.append(": ");
            if (cards.isEmpty()) {
                line.append("(no cards committed)");
            } else {
                for (int j = 0; j < cards.size(); j++) {
                    if (j > 0) line.append(", ");
                    line.append(((Card) cards.get(j)).getTitle());
                }
                line.append("  (total ").append(c.playerTotal(pl)).append(")");
            }
            participantListModel.addElement(line.toString());
        }
        if (joiningConflict && human != null && !c.isSupporting(human) && !c.isOpposing(human)) {
            participantListModel.addElement("-- choose Support or Oppose --");
        }
        if (participantListModel.isEmpty()) {
            participantListModel.addElement("(no participants yet)");
        }
    }


    /**
     * B5-0328 F4: single authority for both split buttons' enablement. Reads
     * live state + current selection; never mutates selection, so it is safe
     * from state refreshes AND from programmatic selector changes.
     */
    private void updatePlayInitiateButtons() {
        GameState st = MainWindow.this.controller.getState();
        if (st.isGameOver()) return;
        boolean myTurn = st.getActivePlayer() == humanPlayer()
            && MainWindow.this.controller.isWaitingForHuman();
        boolean activeConflict = st.getActiveConflict() != null;
        GamePhase phase = st.getPhase();
        boolean conflictSelected = (selectedCard instanceof ConflictCard);
        boolean phaseAllowsAction = (phase == GamePhase.ACTION
            || phase == GamePhase.CONFLICT_RESOLUTION
            || phase == GamePhase.AFTERMATH
            || phase == GamePhase.DRAW);
        boolean canInitiate = myTurn && conflictSelected && !activeConflict
            && phaseAllowsAction;
        // B5-0325 F2: when the selector is enabled for a conflict card, require
        // an explicit target so Initiate can't fire on a stale auto-fallback.
        boolean targetReady = !conflictSelected || !targetSelector.isEnabled()
            || selectedTarget != null;
        boolean agendaNeedsLifecycleAction = selectedCard instanceof AgendaCard
            && !rules.canSponsorAgenda(humanPlayer(), (AgendaCard) selectedCard);
        boolean canPlay = myTurn && selectedCard != null && !conflictSelected
            && !agendaNeedsLifecycleAction && phaseAllowsAction;
        playCardOnlyButton.setEnabled(canPlay);
        initiateConflictButton.setEnabled(canInitiate && targetReady);
        refreshAgendaControls(humanPlayer(), myTurn && phase == GamePhase.ACTION);
    }

    /** B5-0328 F4: dispatch for the "Play Card" button (never initiates). */
    private void playOnly() {
        if (selectedCard == null || selectedCard instanceof ConflictCard) return;
        MainWindow.this.controller.submitHumanAction(GameAction.playCard(selectedCard));
        clearSelection();
    }

    /** B5-0328 F4: dispatch for the "Initiate Conflict" button (never plays). */
    private void initiateOnly() {
        if (!(selectedCard instanceof ConflictCard)) return;
        // B5-0325 F2: human-selected target, auto-target fallback.
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
        if (target == null) return;
        MainWindow.this.controller.submitHumanAction(
            GameAction.initiateConflict(selectedCard, target));
        clearSelection();
    }

    /** B5-0328 F4: drop the selection after a submit; buttons stay disabled. */
    private void clearSelection() {
        selectedCard = null;
        selectedTarget = null;
        selectedFleet = null;
        selectedAssistant = null;
        targetSelector.setSelectedIndex(-1);
        targetSelector.setEnabled(false);
        if (leadFleetSelector != null) {
            leadFleetSelector.setSelectedIndex(-1);
            leadFleetSelector.setEnabled(false);
        }
        if (rotateEffectKindSelector != null) {
            rotateEffectKindSelector.setSelectedIndex(0);
            rotateEffectKindSelector.setEnabled(false);
        }
        playCardOnlyButton.setEnabled(false);
        initiateConflictButton.setEnabled(false);
        leadFleetButton.setEnabled(false);
        useRotateEffectButton.setEnabled(false);
        attackButton.setEnabled(false);
        healButton.setEnabled(false);
        repairButton.setEnabled(false);
    }

    /**
     * B5-0331a: true when candidate is a "Round N" log prefix (N = digits).
     * Char-scanning instead of a regex literal so backslash escaping can
     * never silently disable it.
     */
    public static boolean isRoundPrefix(String candidate) {
        if (candidate == null || !candidate.startsWith("Round ")) return false;
        String rest = candidate.substring(6).trim();
        if (rest.length() == 0) return false;
        for (int i = 0; i < rest.length(); i++) {
            char c = rest.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    /**
     * B5-0331a: true when candidate is a "Phase X" log prefix (X = word
     * characters). Char-scanning instead of a regex literal so backslash
     * escaping can never silently disable it.
     */
    public static boolean isPhasePrefix(String candidate) {
        if (candidate == null || !candidate.startsWith("Phase ")) return false;
        String rest = candidate.substring(6).trim();
        if (rest.length() == 0) return false;
        for (int i = 0; i < rest.length(); i++) {
            char c = rest.charAt(i);
            boolean wordChar = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9') || c == '_';
            if (!wordChar) return false;
        }
        return true;
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

    // B5-0348: tiny checkbox for filter panel
    private JCheckBox makeTinyCheckBox(String label) {
        JCheckBox cb = new JCheckBox(label);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 9));
        cb.setFocusPainted(false);
        cb.setForeground(new Color(200, 220, 200));
        cb.setBackground(new Color(10, 20, 10));
        return cb;
    }
}
