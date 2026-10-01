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

    // B5-0701: unconditional-surrender availability readout (B5-0661 landed the
    // engine law; nothing in the UI surfaced it). READOUT ONLY in the sense that
    // the engine remains the sole authority: this panel never re-derives
    // legality, it calls RulesEngine.canSurrender and renders the answer. The
    // +3 influence consequence shown here is the rulebook :817 grant, which
    // goes to the TARGET, not to the surrendering player.
    private JComboBox<String> surrenderTargetSelector;
    private JButton          surrenderButton;
    private JLabel           surrenderHintLabel;
    private boolean          surrenderSelectorPopulating = false;

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
    // B5-0452 (B5-0451 F1): suppresses the target-selector listener while the
    // selector is being (re)populated — a JComboBox auto-selects its first
    // item and fires the listener during population, which set selectedTarget
    // programmatically and satisfied the B5-0325 explicit-target gate without
    // any user action.
    private boolean targetSelectorPopulating = false;

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

    // B5-0487: Censure opponent-fleet target picker. When the human selects an
    // Enhancement FLEET card (Censure-class) from hand, this dropdown lists the
    // opponent factions' face-up fleets; picking one sets the explicit opponent
    // target via the B5-0468 seam before the card is played. If no target is
    // picked (or none are legal), the engine's held-in-play policy applies —
    // no self-fallback onto the owner's own fleet.
    private JComboBox<String> censureTargetSelector;
    private JLabel             censureTargetLabel;
    private JButton            censurePlayButton;
    private final java.util.ArrayList<FleetCard> censureTargetCards = new java.util.ArrayList<FleetCard>();
    private final java.util.ArrayList<String>    censureTargetOwnerNames = new java.util.ArrayList<String>();
    private FleetCard selectedCensureTarget;
    private String    selectedCensureTargetOwner;

    // B5-0522: opponent-CHARACTER target picker (shunned-class today). Mirrors
    // the B5-0487 fleet picker: when the human selects an Enhancement CHARACTER
    // card carrying the B5-0468 explicit-target seam (shunned-class today),
    // this dropdown lists opponent factions' face-up characters (inner circle,
    // supporting role, ambassador — the B5-0506 characterById scope); picking
    // one sets the explicit opponent target via the seam before the card is
    // played. If no target is picked (or none are legal), the engine's
    // held-in-play policy applies — no self-fallback onto the owner's own
    // character. Non-targeted CHARACTER enhancements keep the normal path.
    private JComboBox<String> charCensureTargetSelector;
    private JLabel             charCensureTargetLabel;
    private JButton            charCensurePlayButton;
    private final java.util.ArrayList<CharacterCard> charCensureTargetCards = new java.util.ArrayList<CharacterCard>();
    private final java.util.ArrayList<String>        charCensureTargetOwnerNames = new java.util.ArrayList<String>();
    private CharacterCard selectedCharCensureTarget;
    private String        selectedCharCensureTargetOwner;

    // B5-0401: Tier-1 remainder action UI — Lead Fleet + Use Rotate Effect
    private JButton leadFleetButton;
    private JComboBox<String> leadFleetSelector;
    private FleetCard selectedFleet;
    private JButton useRotateEffectButton;
    private JComboBox<String> rotateEffectKindSelector;
    private CharacterCard selectedAssistant;

    // B5-0402: Tier-3 action UI — Attack + Heal + Repair
    private JButton attackButton;
    private JComboBox<String> attackTargetSelector;
    private final java.util.ArrayList<Card> attackTargetCards = new java.util.ArrayList<Card>();
    private Card selectedAttackTarget;
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
                if (MainWindow.this.controller.isWaitingForHumanConflictAttack()) {
                    clearSelection();
                }
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

        // B5-0701: surrender availability. Target-first, then commit: surrender
        // needs a TARGET player, so a single button with no selector would have
        // nothing legal to submit. The selector lists only players the engine
        // currently accepts, so an empty list IS the "not available" signal.
        surrenderTargetSelector = new JComboBox<String>();
        surrenderTargetSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (surrenderSelectorPopulating) return;
                refreshSurrenderButton();
            }
        });
        surrenderTargetSelector.setEnabled(false);

        surrenderButton = makeButton("Surrender", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player hp = humanPlayer();
                Player target = selectedSurrenderTarget();
                if (hp == null || target == null) return;
                // Engine is the authority: re-check at the commit point, exactly
                // as the sponsor/promote/build-influence handlers do, so a stale
                // enabled button can never submit an illegal action.
                if (rules.canSurrender(hp, target,
                        MainWindow.this.controller.getState())) {
                    MainWindow.this.controller.submitHumanAction(
                        GameAction.surrender(target));
                }
            }
        });
        surrenderButton.setEnabled(false);
        surrenderButton.setToolTipText(
            "Unconditionally surrender (rulebook :815). You go out of the game; "
            + "the target gains 3 influence and receives your ambassador as an "
            + "asylum character.");

        surrenderHintLabel = new JLabel(" ");
        // NOTE: added to the toolbar in the assembly block below, beside the
        // other B5-0407-style target+button pairs, not here.

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
                // B5-0452 (B5-0451 F1): ignore events fired by programmatic
                // population — only a real user change may set the target.
                if (targetSelectorPopulating) return;
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
                if (!MainWindow.this.controller.isWaitingForHumanConflictAttack()
                        || hp == null || conflict == null || selectedCard == null
                        || selectedAttackTarget == null) return;
                Card attacker = selectedCard;
                if (rules.canAttackConflictParticipant(hp, attacker, selectedAttackTarget, conflict)) {
                    MainWindow.this.controller.submitHumanAction(
                        GameAction.attackConflictParticipant(attacker, selectedAttackTarget));
                    clearSelection();
                }
            }
        });
        attackButton.setEnabled(false);
        attackButton.setToolTipText("Attack a selected participant in the live conflict window (B5-0370).");

        attackTargetSelector = new JComboBox<String>(new String[] { "(select target)" });
        attackTargetSelector.setEnabled(false);
        attackTargetSelector.setMaximumSize(new Dimension(220, 24));
        attackTargetSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = attackTargetSelector.getSelectedIndex();
                selectedAttackTarget = index > 0 && index - 1 < attackTargetCards.size()
                    ? attackTargetCards.get(index - 1) : null;
                updateAttackButton();
            }
        });

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

        // B5-0487: Censure opponent-fleet target picker
        censureTargetSelector = new JComboBox<String>(new String[] { "(select opponent fleet)" });
        censureTargetSelector.setEnabled(false);
        censureTargetSelector.setMaximumSize(new Dimension(200, 24));
        censureTargetSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCensurePlayButton();
            }
        });

        censureTargetLabel = new JLabel("Opponent fleet target:");
        censureTargetLabel.setForeground(new Color(200, 220, 200));
        censureTargetLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        censureTargetLabel.setMaximumSize(new Dimension(140, 24));

        censurePlayButton = makeButton("Play (opponent target)", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player hp = humanPlayer();
                if (hp == null) return;
                if (!(selectedCard instanceof EnhancementCard)) return;
                EnhancementCard enh = (EnhancementCard) selectedCard;
                if (!(enh.getSubtype() != null && enh.getSubtype().endsWith("_FLEET"))) return;
                // B5-0522 same-class fix (B5-0435/0490 precedent, disclosed):
                // route on the PICKER's selection, not enh.hasExplicitTarget() —
                // the seam is set inside playCensureWithTarget right before
                // submit, so the old check was never true and every targeted
                // click fell through to the self-target path.
                if (selectedCensureTarget == null || selectedCensureTargetOwner == null) {
                    // No opponent fleet picked (button should be disabled) —
                    // normal self-target play path.
                    MainWindow.this.controller.submitHumanAction(GameAction.playCard(enh));
                    clearSelection();
                    return;
                }
                // Explicit opponent target chosen — submit with the target
                playCensureWithTarget();
            }
        });
        censurePlayButton.setEnabled(false);
        censurePlayButton.setToolTipText("Play this Enhancement targeting the selected opponent fleet (B5-0487).");

        // B5-0522: opponent-CHARACTER target picker (shunned-class seam cards)
        charCensureTargetSelector = new JComboBox<String>(new String[] { "(select opponent character)" });
        charCensureTargetSelector.setEnabled(false);
        charCensureTargetSelector.setMaximumSize(new Dimension(200, 24));
        charCensureTargetSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCharCensurePlayButton();
            }
        });

        charCensureTargetLabel = new JLabel("Opponent character target:");
        charCensureTargetLabel.setForeground(new Color(200, 220, 200));
        charCensureTargetLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        charCensureTargetLabel.setMaximumSize(new Dimension(150, 24));

        charCensurePlayButton = makeButton("Play (opponent char target)", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player hp = humanPlayer();
                if (hp == null) return;
                if (!(selectedCard instanceof EnhancementCard)) return;
                EnhancementCard enh = (EnhancementCard) selectedCard;
                if (!(enh.getSubtype() != null && enh.getSubtype().endsWith("_CHARACTER"))) return;
                // B5-0522: route on the PICKER's selection, not enh
                // hasExplicitTarget() — the seam is set by playCharCensureWithTarget
                // itself right before submit, so a fresh hand card never tests true
                // here. (The B5-0487 fleet handler's hasExplicitTarget() check sent
                // every targeted click down the self-path; same-class fix applied
                // to that handler in this same claim, disclosed in the close-out.)
                if (selectedCharCensureTarget == null || selectedCharCensureTargetOwner == null) {
                    // No opponent character picked (button should be disabled) —
                    // normal self-target play path.
                    MainWindow.this.controller.submitHumanAction(GameAction.playCard(enh));
                    clearSelection();
                    return;
                }
                // Explicit opponent target chosen — submit with the target
                playCharCensureWithTarget();
            }
        });
        charCensurePlayButton.setEnabled(false);
        charCensurePlayButton.setToolTipText("Play this Enhancement targeting the selected opponent character (B5-0522).");

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
        toolbar.add(attackTargetSelector);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(attackButton);
        toolbar.add(healButton);
        toolbar.add(repairButton);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(mercenaryBidAmountSelector);
        toolbar.add(mercenaryBidButton);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(mercenaryOfferLabel);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(censureTargetLabel);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(censureTargetSelector);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(censurePlayButton);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(charCensureTargetLabel);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(charCensureTargetSelector);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(charCensurePlayButton);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(warTargetSelector);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(declareWarButton);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(warStatusLabel);
        toolbar.add(Box.createHorizontalStrut(12));
        // B5-0701: surrender availability readout. Sits with the other
        // target+button pairs; the hint label carries the +3-influence
        // consequence readout the row asks for.
        toolbar.add(surrenderTargetSelector);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(surrenderButton);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(surrenderHintLabel);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(initiativeLabel);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(statusLabel);

        add(toolbar, BorderLayout.NORTH);

        // Ã¢â€â‚¬Ã¢â€â‚¬ Hand Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
        handPanel = new HandPanel();
        handPanel.setOnCardSelected(new CardSelectedListener() {
            @Override
            public void onCardSelected(Card card) {
                applyCardSelection(card, true);
            }
        });
        // B5-0423: board-side selection. The heal/repair/lead-fleet/rotate-effect
        // controls read selectedCard, but those cards (IC, supporting, fleets,
        // locations) never appear in a hand — without this the buttons could
        // never light for a legal target. Shares applyCardSelection with the
        // hand so target-population and enablement rules stay identical.
        boardPanel.setOnBoardCardSelected(new CardSelectedListener() {
            @Override
            public void onCardSelected(Card card) {
                applyCardSelection(card, false);
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

    private void refreshAttackControl(Player human, boolean attackWindow) {
        Card previous = selectedAttackTarget;
        selectedAttackTarget = null;
        attackTargetCards.clear();
        attackTargetSelector.removeAllItems();
        attackTargetSelector.addItem("(select target)");
        if (!attackWindow || human == null || selectedCard == null) {
            attackTargetSelector.setEnabled(false);
            attackButton.setEnabled(false);
            return;
        }
        Conflict conflict = MainWindow.this.controller.getState().getActiveConflict();
        if (conflict == null) {
            attackTargetSelector.setEnabled(false);
            attackButton.setEnabled(false);
            return;
        }
        java.util.ArrayList<Player> participants =
            new java.util.ArrayList<Player>(conflict.getParticipants());
        for (int i = 0; i < participants.size(); i++) {
            Player participant = participants.get(i);
            if (participant == human) continue;
            java.util.ArrayList<Card> committed =
                new java.util.ArrayList<Card>(conflict.getCommittedCards(participant));
            for (int j = 0; j < committed.size(); j++) {
                Card target = committed.get(j);
                if (rules.canAttackConflictParticipant(human, selectedCard, target, conflict)) {
                    attackTargetCards.add(target);
                    attackTargetSelector.addItem(participant.getName() + " - " + target.getTitle());
                }
            }
        }
        if (attackTargetCards.isEmpty()) {
            attackTargetSelector.setEnabled(false);
            attackButton.setEnabled(false);
            return;
        }
        int previousIndex = previous == null ? -1 : attackTargetCards.indexOf(previous);
        if (previousIndex >= 0) {
            attackTargetSelector.setSelectedIndex(previousIndex + 1);
            selectedAttackTarget = previous;
        }
        attackTargetSelector.setEnabled(true);
        updateAttackButton();
    }

    private void updateAttackButton() {
        Player human = humanPlayer();
        Conflict conflict = MainWindow.this.controller.getState().getActiveConflict();
        boolean canAttack = MainWindow.this.controller.isWaitingForHumanConflictAttack()
            && human != null && conflict != null && selectedCard != null
            && selectedAttackTarget != null
            && rules.canAttackConflictParticipant(human, selectedCard, selectedAttackTarget, conflict);
        attackButton.setEnabled(canAttack);
    }

    /** B5-0326 F5: show preview of the cost for the currently selected card. */
    private void refreshCostPreview() {
        Player hp = humanPlayer();
        if (hp == null) {
            costLabel.setText("  ");
            return;
        }
        GamePhase phase = MainWindow.this.controller.getState().getPhase();
        if (phase != GamePhase.ACTION) {
            costLabel.setText("  ");
            return;
        }
        StringBuilder sb = new StringBuilder();
        // B5-1049: surface the generic-play cost gate in the UI
        // Show unaffordable cards (non-character cards with cost that player cannot afford)
        java.util.List<String> unaffordableCards = new java.util.ArrayList<String>();
        for (Card c : hp.getHand()) {
            // Skip characters - they have sponsor/promote costs handled in B5-0326 section
            if (c instanceof CharacterCard) continue;
            if (!c.getFaction().isPlayableBy(hp.getFaction())) continue;
            int cost = c.getCost();
            if (cost > 0 && hp.getAppliedPool() < cost) {
                // B5-1038: affordability precheck - card is in hand but cannot afford the cost
                unaffordableCards.add(c.getTitle() + " (" + cost + " INF need)");
            }
        }
        if (!unaffordableCards.isEmpty()) {
            sb.append("UNAFFORDABLE: ");
            for (int i = 0; i < unaffordableCards.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(unaffordableCards.get(i));
            }
            sb.append("  |  ");
        }
        // B5-0326 F5: show preview of the cost for the currently selected character card
        CharacterCard ch = (selectedCard instanceof CharacterCard)
            ? (CharacterCard) selectedCard : null;
        if (ch == null && sb.length() == 0) {
            // No character selected AND no unaffordable cards - clear display
            costLabel.setText("  ");
            return;
        }
        // Sponsor (RECRUIT) cost
        if (ch != null && hp.getHand().contains(ch) && !ch.isRotated() && !ch.isFaceDown()) {
            int rc = rules.recruitCost(hp, ch);
            sb.append("Sponsor cost: " + rc + "  |  ");
        }
        // Promote cost
        if (ch != null && hp.getSupportingRole().contains(ch) && !ch.isRotated() && !ch.isFaceDown()) {
            int pc = rules.promotionCost(hp, ch);
            sb.append("Promote cost: " + pc + "  |  ");
        }
        // Build Influence not card-specific, but show if available
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
        boolean attackWindow = activeConflict
            && MainWindow.this.controller.isWaitingForHumanConflictAttack();
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

        passButton.setText(attackWindow ? "Skip Attack" : "Pass Turn");
        passButton.setEnabled(myTurn || attackWindow);

        // B5-0328 F4: split Play/Initiate Ã¢â‚¬â€ enablement lives in one authority,
        // updatePlayInitiateButtons(); dispatch in playOnly() / initiateOnly().
        updatePlayInitiateButtons();

        // B5-0327 F8: initiative order display — show active player as the current
        // initiative holder. The human player's initiative position is tracked in the
        // status bar for clarity; the full initiative chain is rendered on the board.
        initiativeLabel.setText("Initiative: " + state.getActivePlayer().getName()
            + (myTurn ? " (you)" : ""));
        boolean actionPhase = (phase == GamePhase.ACTION);
        CharacterCard ch = (selectedCard instanceof CharacterCard)
            ? (CharacterCard) selectedCard : null;

        // B5-0487: Censure opponent-fleet target picker
        refreshCensureControl(human, actionPhase && myTurn);

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

        // B5-0701: surrender availability. The engine decides; this only renders.
        refreshSurrenderControl(human);

        // B5-0402: Attack - enabled only for a selected legal pair in the live conflict window
        Card selCard = selectedCard;
        refreshAttackControl(human, attackWindow);

        // Heal / Repair: the engine is the authority (B5-0423). The action
        // handlers already gate on rules.canHealCharacter / canRepairCard, so
        // duplicating a partial predicate here only let the two drift — the
        // hand-rolled version omitted !isRotated and the undamaged-IC aid
        // path, so it disabled a legal move. One predicate, one answer.
        boolean canHeal = actionPhase && myTurn && ch != null
            && rules.canHealCharacter(human, ch);
        healButton.setEnabled(canHeal);

        boolean canRepair = actionPhase && myTurn && selCard != null
            && (selCard instanceof FleetCard || selCard instanceof LocationCard)
            && rules.canRepairCard(human, selCard);
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

        // B5-0487: Censure opponent-fleet target picker
        refreshCensureControl(human, actionPhase && myTurn);

        // B5-0404: mercenary bid control
        refreshMercenaryBidControl(human, actionPhase && myTurn);

        // B5-0407: declare-war control
        refreshDeclareWarControl(human, actionPhase && myTurn);

        // B5-0487: Censure opponent-fleet target picker
        refreshCensureControl(human, actionPhase && myTurn);

        // B5-0326 F5: refresh cost preview
        refreshCostPreview();

        if (state.isGameOver()) {
            VictoryPathResult vpr = rules.checkVictoryPath(state);
            if (vpr != null && vpr.getPath() != null) {
                StringBuilder sb = new StringBuilder("GAME OVER — ");
                sb.append(vpr.getPath()).append(" win");
                sb.append("  |  Power: ").append(vpr.getPower());
                switch (vpr.getPath()) {
                    case AGENDA_CONDITION:
                        sb.append("  |  Agenda: ").append(vpr.getQualifierAsString());
                        break;
                    case LAST_STANDING:
                        sb.append("  |  Remaining: ").append(vpr.getQualifierAsInt());
                        break;
                    case STATION_CONDITION_2:
                        sb.append("  |  Station: ").append(vpr.getQualifierAsInt());
                        break;
                    case MAJOR:
                    case STANDARD:
                        sb.append("  |  Lead: ").append(vpr.getQualifierAsInt());
                        break;
                }
                statusLabel.setText(sb.toString());
            } else {
                statusLabel.setText("GAME OVER — No winner");
            }
            passButton.setEnabled(false);
            playCardOnlyButton.setEnabled(false);
            initiateConflictButton.setEnabled(false);
        } else {
            if (attackWindow) {
                statusLabel.setText("Round " + state.getRoundNumber()
                    + "  |  Conflict: select attacker and target, or Pass");
            } else if (joiningConflict) {
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

    // ── B5-0701: unconditional surrender availability (ui/, readout only) ──────

    /**
     * B5-0701: refresh the surrender control from the engine's own verdict.
     *
     * Design note — the engine is the ONLY authority here. This method does not
     * re-derive any part of the legality rule (phase, surrender/forfeit flags,
     * self-target, at-war, ambassador-in-play). It asks
     * {@link RulesEngine#canSurrender} once per candidate and renders the
     * answer, for the same reason the B5-0423 heal/repair controls were changed:
     * a hand-rolled partial predicate here is what let those two drift and
     * disable a legal move.
     *
     * An empty candidate list IS the "not available" signal, which is why the
     * readout is honest by construction — it cannot show a stale "available"
     * for a state the engine has since rejected.
     */
    private void refreshSurrenderControl(Player human) {
        if (surrenderTargetSelector == null || surrenderButton == null) return;
        GameState state = controller.getState();

        java.util.List<Player> legal = new java.util.ArrayList<Player>();
        if (human != null && !human.hasSurrendered() && !human.hasForfeited()) {
            for (Player p : state.getPlayers()) {
                if (rules.canSurrender(human, p, state)) legal.add(p);
            }
        }

        // Repopulate under the B5-0452 guard: a JComboBox auto-selects its
        // first item and fires the listener, which would otherwise set a
        // selection with no user action.
        surrenderSelectorPopulating = true;
        try {
            surrenderTargetSelector.removeAllItems();
            for (Player p : legal) {
                surrenderTargetSelector.addItem(p.getName() + " (+3)");
            }
            if (legal.isEmpty()) {
                surrenderTargetSelector.addItem("(surrender unavailable)");
                surrenderTargetSelector.setSelectedIndex(0);
                surrenderTargetSelector.setEnabled(false);
            } else {
                surrenderTargetSelector.setSelectedIndex(0);
                surrenderTargetSelector.setEnabled(true);
            }
        } finally {
            surrenderSelectorPopulating = false;
        }

        surrenderButton.setEnabled(!legal.isEmpty());
        refreshSurrenderHint(legal);
    }

    /**
     * B5-0701: the +3-influence consequence readout. The grant goes to the
     * TARGET (rulebook :817), so the label states the direction explicitly —
     * "you gain 3" would be a lie, and this is exactly the kind of detail a
     * readout exists to get right.
     */
    private void refreshSurrenderHint(java.util.List<Player> legal) {
        if (surrenderHintLabel == null) return;
        if (legal.isEmpty()) {
            surrenderHintLabel.setText(" ");
            surrenderHintLabel.setToolTipText(
                "Surrender is offered only during the draw round, to a race you "
                + "are at war with, and requires an ambassador in play "
                + "(rulebook :815-:819).");
            return;
        }
        Player t = legal.get(0);
        surrenderHintLabel.setText(t.getName() + " gains +3 inf");
        surrenderHintLabel.setToolTipText(
            "Surrendering ends YOUR game. " + t.getName() + " gains 3 influence "
            + "(" + t.getInfluence() + " -> " + (t.getInfluence() + 3) + ") and "
            + "receives your ambassador as an asylum character.");
    }

    /** B5-0701: enable/disable the commit button from the current selection. */
    private void refreshSurrenderButton() {
        if (surrenderButton == null) return;
        Player hp = humanPlayer();
        Player target = selectedSurrenderTarget();
        surrenderButton.setEnabled(hp != null && target != null
            && rules.canSurrender(hp, target, controller.getState()));
    }

    /** B5-0701: resolve the selected combo entry back to its Player. */
    private Player selectedSurrenderTarget() {
        if (surrenderTargetSelector == null) return null;
        Object sel = surrenderTargetSelector.getSelectedItem();
        if (sel == null) return null;
        String label = sel.toString();
        int plus = label.indexOf(" (+3)");
        String name = (plus >= 0) ? label.substring(0, plus) : label;
        if (name.startsWith("(")) return null;   // placeholder entry
        for (Player p : controller.getState().getPlayers()) {
            if (p.getName().equals(name)) return p;
        }
        return null;
    }

    /** B5-0487: refresh the Censure opponent-fleet target picker from current state. */
    private void refreshCensureControl(Player human, boolean actionTurn) {
        // B5-0522: the parallel opponent-character picker refreshes alongside.
        refreshCharCensureControl(human, actionTurn);
        if (human == null || censureTargetSelector == null) {
            if (censureTargetSelector != null) {
                censureTargetSelector.setEnabled(false);
            }
            censurePlayButton.setEnabled(false);
            return;
        }
        // Only relevant when an Enhancement FLEET card is selected and it carries
        // the explicit-target seam (Censure-class today).
        if (selectedCard == null || !(selectedCard instanceof EnhancementCard)) {
            censureTargetSelector.setEnabled(false);
            censurePlayButton.setEnabled(false);
            censureTargetLabel.setText("");
            return;
        }
        EnhancementCard enh = (EnhancementCard) selectedCard;
        if (enh.getSubtype() == null || !enh.getSubtype().endsWith("_FLEET")) {
            censureTargetSelector.setEnabled(false);
            censurePlayButton.setEnabled(false);
            censureTargetLabel.setText("");
            return;
        }
        // Populate opponent fleets — face-up, unrotated fleets owned by non-human players.
        censureTargetCards.clear();
        censureTargetOwnerNames.clear();
        censureTargetSelector.removeAllItems();
        censureTargetSelector.addItem("(select opponent fleet)");
        GameState gs = MainWindow.this.controller.getState();
        for (Player p : gs.getPlayers()) {
            if (p.isHuman() || p.hasForfeited()) continue;
            for (FleetCard fl : p.getFleets()) {
                if (fl.isFaceDown() || fl.isRotated()) continue;
                censureTargetCards.add(fl);
                censureTargetOwnerNames.add(p.getName());
                censureTargetSelector.addItem(p.getName() + " — " + fl.getTitle());
            }
        }
        if (censureTargetCards.isEmpty()) {
            censureTargetSelector.setEnabled(false);
            censurePlayButton.setEnabled(false);
            censureTargetLabel.setText("No opponent fleets available.");
            return;
        }
        // Restore previous selection if it is still present.
        if (selectedCensureTarget != null) {
            int idx = censureTargetCards.indexOf(selectedCensureTarget);
            if (idx >= 0) {
                censureTargetSelector.setSelectedIndex(idx + 1);
            } else {
                censureTargetSelector.setSelectedIndex(0);
                selectedCensureTarget = null;
                selectedCensureTargetOwner = null;
            }
        } else {
            censureTargetSelector.setSelectedIndex(0);
        }
        censureTargetSelector.setEnabled(actionTurn);
        updateCensurePlayButton();
    }

    /**
     * B5-0522: refresh the opponent-CHARACTER target picker from current state.
     * Mirrors refreshCensureControl (B5-0487) for Enhancement CHARACTER cards
     * carrying the B5-0468 seam (shunned-class today). Reads only: face-up
     * characters across inner circle, supporting role, and ambassador of every
     * non-human, non-forfeited player — the B5-0506 characterById resolution
     * scope, so anything listed here is resolvable by the engine path.
     */
    private void refreshCharCensureControl(Player human, boolean actionTurn) {
        if (human == null || charCensureTargetSelector == null) {
            if (charCensureTargetSelector != null) {
                charCensureTargetSelector.setEnabled(false);
            }
            if (charCensurePlayButton != null) {
                charCensurePlayButton.setEnabled(false);
            }
            return;
        }
        if (selectedCard == null || !(selectedCard instanceof EnhancementCard)) {
            charCensureTargetSelector.setEnabled(false);
            charCensurePlayButton.setEnabled(false);
            charCensureTargetLabel.setText("");
            return;
        }
        EnhancementCard enh = (EnhancementCard) selectedCard;
        if (enh.getSubtype() == null || !enh.getSubtype().endsWith("_CHARACTER")) {
            charCensureTargetSelector.setEnabled(false);
            charCensurePlayButton.setEnabled(false);
            charCensureTargetLabel.setText("");
            return;
        }
        // Populate opponent characters — face-up, owned by non-human players.
        // Scope mirrors CardEffects.characterById (B5-0506): inner circle,
        // supporting role, then ambassador. No rotate check — the engine
        // resolves characters by face-up only.
        charCensureTargetCards.clear();
        charCensureTargetOwnerNames.clear();
        charCensureTargetSelector.removeAllItems();
        charCensureTargetSelector.addItem("(select opponent character)");
        GameState gs = MainWindow.this.controller.getState();
        for (Player p : gs.getPlayers()) {
            if (p.isHuman() || p.hasForfeited()) continue;
            for (CharacterCard ch : p.getInnerCircle()) {
                if (ch.isFaceDown()) continue;
                charCensureTargetCards.add(ch);
                charCensureTargetOwnerNames.add(p.getName());
                charCensureTargetSelector.addItem(p.getName() + " — " + ch.getTitle());
            }
            for (CharacterCard ch : p.getSupportingRole()) {
                if (ch.isFaceDown()) continue;
                charCensureTargetCards.add(ch);
                charCensureTargetOwnerNames.add(p.getName());
                charCensureTargetSelector.addItem(p.getName() + " — " + ch.getTitle());
            }
            CharacterCard amb = p.getAmbassador();
            if (amb != null && !amb.isFaceDown()) {
                charCensureTargetCards.add(amb);
                charCensureTargetOwnerNames.add(p.getName());
                charCensureTargetSelector.addItem(p.getName() + " — " + amb.getTitle());
            }
        }
        if (charCensureTargetCards.isEmpty()) {
            charCensureTargetSelector.setEnabled(false);
            charCensurePlayButton.setEnabled(false);
            charCensureTargetLabel.setText("No opponent characters available.");
            return;
        }
        // Restore previous selection if it is still present.
        if (selectedCharCensureTarget != null) {
            int idx = charCensureTargetCards.indexOf(selectedCharCensureTarget);
            if (idx >= 0) {
                charCensureTargetSelector.setSelectedIndex(idx + 1);
            } else {
                charCensureTargetSelector.setSelectedIndex(0);
                selectedCharCensureTarget = null;
                selectedCharCensureTargetOwner = null;
            }
        } else {
            charCensureTargetSelector.setSelectedIndex(0);
        }
        charCensureTargetSelector.setEnabled(actionTurn);
        updateCharCensurePlayButton();
    }

    /** B5-0522: enable the character-target play button when a character is explicitly selected. */
    private void updateCharCensurePlayButton() {
        if (charCensureTargetSelector == null || charCensurePlayButton == null) return;
        int idx = charCensureTargetSelector.getSelectedIndex();
        boolean hasSelection = idx > 0 && idx - 1 < charCensureTargetCards.size();
        if (hasSelection) {
            selectedCharCensureTarget = charCensureTargetCards.get(idx - 1);
            selectedCharCensureTargetOwner = charCensureTargetOwnerNames.get(idx - 1);
        } else {
            selectedCharCensureTarget = null;
            selectedCharCensureTargetOwner = null;
        }
        // Must be ACTION phase, my turn, and an opponent character is
        // explicitly selected (the target seam is set BY the user's pick, so
        // gating on hasExplicitTarget() here would never enable — same reason
        // as the B5-0487 fleet picker).
        Player hp = humanPlayer();
        GameState gs = MainWindow.this.controller.getState();
        boolean actionTurn = gs.getPhase() == GamePhase.ACTION
            && gs.getActivePlayer() == hp
            && MainWindow.this.controller.isWaitingForHuman();
        charCensurePlayButton.setEnabled(actionTurn && hasSelection);
        charCensureTargetLabel.setText(hasSelection
            ? "Target: " + selectedCharCensureTargetOwner + " — " + selectedCharCensureTarget.getTitle()
            : "Opponent character target:");
    }

    /** B5-0522: play the selected Enhancement targeting the chosen opponent character. */
    private void playCharCensureWithTarget() {
        if (selectedCharCensureTarget == null || selectedCharCensureTargetOwner == null) return;
        if (!(selectedCard instanceof EnhancementCard)) return;
        EnhancementCard enh = (EnhancementCard) selectedCard;
        // Set the explicit opponent target via the B5-0468 seam.
        enh.setOpponentTarget(selectedCharCensureTarget.getId(), selectedCharCensureTargetOwner);
        MainWindow.this.controller.submitHumanAction(GameAction.playCard(enh));
        clearSelection();
    }

    /** B5-0487: enable the Censure play button when a fleet is explicitly selected. */
    private void updateCensurePlayButton() {
        if (censureTargetSelector == null || censurePlayButton == null) return;
        int idx = censureTargetSelector.getSelectedIndex();
        boolean hasSelection = idx > 0 && idx - 1 < censureTargetCards.size();
        if (hasSelection) {
            selectedCensureTarget = censureTargetCards.get(idx - 1);
            selectedCensureTargetOwner = censureTargetOwnerNames.get(idx - 1);
        } else {
            selectedCensureTarget = null;
            selectedCensureTargetOwner = null;
        }
        // Must be ACTION phase, my turn, and an opponent fleet is explicitly
        // selected (the target seam is set BY the user's pick, so gating on
        // hasExplicitTarget() here would never enable — the card has no target
        // until the picker fires).
        Player hp = humanPlayer();
        GameState gs = MainWindow.this.controller.getState();
        boolean actionTurn = gs.getPhase() == GamePhase.ACTION
            && gs.getActivePlayer() == hp
            && MainWindow.this.controller.isWaitingForHuman();
        censurePlayButton.setEnabled(actionTurn && hasSelection);
        censureTargetLabel.setText(hasSelection
            ? "Target: " + selectedCensureTargetOwner + " — " + selectedCensureTarget.getTitle()
            : "Opponent fleet target:");
    }

    /** B5-0487: play the selected Enhancement targeting the chosen opponent fleet. */
    private void playCensureWithTarget() {
        if (selectedCensureTarget == null || selectedCensureTargetOwner == null) return;
        if (!(selectedCard instanceof EnhancementCard)) return;
        EnhancementCard enh = (EnhancementCard) selectedCard;
        // Set the explicit opponent target via the B5-0468 seam.
        enh.setOpponentTarget(selectedCensureTarget.getId(), selectedCensureTargetOwner);
        MainWindow.this.controller.submitHumanAction(GameAction.playCard(enh));
        clearSelection();
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
        // B5-0458 (B5-0451 F4): initiation is an ACTION-phase act, so the
        // Initiate Conflict button now gates on ACTION only. The broader
        // phaseAllowsAction above stays for Play Card (inherited draft
        // breadth; tightening Play was explicitly out of F4's scope and
        // remains out of ours).
        boolean phaseAllowsInitiation = (phase == GamePhase.ACTION);
        boolean canInitiate = myTurn && conflictSelected && !activeConflict
            && phaseAllowsInitiation;
        // B5-0325 F2: when the selector is enabled for a conflict card, require
        // an explicit target so Initiate can't fire on a stale auto-fallback.
        boolean targetReady = !conflictSelected || !targetSelector.isEnabled()
            || selectedTarget != null;
        boolean agendaNeedsLifecycleAction = selectedCard instanceof AgendaCard
            && !rules.canSponsorAgenda(humanPlayer(), (AgendaCard) selectedCard);
        // B5-0423: Play reads a card out of the hand, so require hand
        // containment. Without it, a board selection (IC / supporting / fleet /
        // location) is a non-ConflictCard and would light Play and dispatch an
        // illegal playCard for a card that is not in hand.
        boolean inHand = selectedCard != null
            && humanPlayer() != null
            && humanPlayer().getHand().contains(selectedCard);
        boolean canPlay = myTurn && inHand && !conflictSelected
            && !agendaNeedsLifecycleAction && phaseAllowsAction
            // B5-1171: the generic Play gate reads no cost and no type route;
            // a lit Play on a character can only reach the corrupting generic
            // dispatch, so characters never light it.
            && !(selectedCard instanceof CharacterCard)
            // B5-1177: affordability precheck for non-conflict cards; the
            // B5-1038 generic play path charges raw cost so unaffordable
            // cards must be excluded at the button, not after click.
            && rules.canPlayCard(humanPlayer(), selectedCard);
        playCardOnlyButton.setEnabled(canPlay);
        initiateConflictButton.setEnabled(canInitiate && targetReady);
        refreshAgendaControls(humanPlayer(), myTurn && phase == GamePhase.ACTION);
    }

    /** B5-0328 F4: dispatch for the "Play Card" button (never initiates). */
    private void playOnly() {
        // B5-1171: refuse characters the way ConflictCard is refused. B5-1109
        // measured that a dispatched CharacterCard reaching
        // applyGenericCardPlay is charged raw cost and discarded without ever
        // entering the supporting role; characters belong to the sponsor and
        // promote buttons, which gate on canRecruit/canPromote.
        if (selectedCard == null || selectedCard instanceof ConflictCard
                || selectedCard instanceof CharacterCard) return;
        // B5-0487: if the selected card is a Censure-class enhancement with an
        // explicit opponent target seam, the censure play button is the dedicated
        // path; the generic Play Card button stays for non-targeted plays.
        if (selectedCard instanceof EnhancementCard) {
            EnhancementCard enh = (EnhancementCard) selectedCard;
            // B5-0522: CHARACTER seam cards (shunned-class) now route through
            // their own picker button too, mirroring the B5-0487 fleet rule.
            if (enh.getSubtype() != null
                    && (enh.getSubtype().endsWith("_FLEET")
                        || enh.getSubtype().endsWith("_CHARACTER"))
                    && enh.hasExplicitTarget()) {
                return;   // censure play buttons own these paths
            }
        }
        MainWindow.this.controller.submitHumanAction(GameAction.playCard(selectedCard));
        clearSelection();
    }

    /** B5-0328 F4: dispatch for the "Initiate Conflict" button (never plays).
     *  B5-0452 (B5-0451 F3): the highest-influence auto-target fallback was
     *  removed — it was unreachable under the targetReady gate (the gate is
     *  false exactly when selectedTarget is null, the only case it covered)
     *  and implied a choice the user never made. The button can only fire
     *  with an explicit user-picked target now. */
    private void initiateOnly() {
        if (!(selectedCard instanceof ConflictCard)) return;
        // B5-0325 F2 as repaired by B5-0452: explicit user-selected target only.
        Player target = selectedTarget;
        if (target == null) return;
        MainWindow.this.controller.submitHumanAction(
            GameAction.initiateConflict(selectedCard, target));
        clearSelection();
    }

    /**
     * B5-0423: shared selection handler for hand and board clicks. `fromHand`
     * only decides whether the human target dropdown is populated — that is a
     * hand-only affordance (a board card is never a conflict to attack), so a
     * board selection leaves the dropdown disabled. Everything downstream
     * (assistant tracking, button enablement, cost preview, Tier-1 remainder
     * controls) is identical for both sources.
     */
    private void applyCardSelection(Card card, boolean fromHand) {
        selectedCard = card;
        refreshAttackControl(humanPlayer(),
            MainWindow.this.controller.isWaitingForHumanConflictAttack());
        if (fromHand && card instanceof ConflictCard) {
            // B5-0452 (B5-0451 F1+F2): repopulate with the listener suppressed
            // so the auto-selection fires nothing, and clear any stale target
            // from a previous card selection — the target must be an explicit
            // user choice for THIS conflict before Initiate enables.
            targetSelectorPopulating = true;
            try {
                targetSelector.removeAllItems();
                for (Player p : controller.getState().getPlayers()) {
                    if (!p.isHuman()) {
                        targetSelector.addItem(p.getName());
                    }
                }
            } finally {
                targetSelectorPopulating = false;
            }
            selectedTarget = null;
            targetSelector.setEnabled(true);
            statusLabel.setText("Selected: " + card.getTitle()
                + "  |  Target: choose from dropdown");
        } else {
            targetSelector.setEnabled(false);
            statusLabel.setText("Selected: " + card.getTitle()
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
        // B5-0401: Refresh Tier-1 remainder action UI
        GameState gs = controller.getState();
        // B5-0487: refresh censure opponent-fleet picker when a card is selected
        refreshCensureControl(hp, gs.getPhase() == GamePhase.ACTION
            && gs.getActivePlayer() == hp && controller.isWaitingForHuman());
        updatePlayInitiateButtons();
        // B5-0326 F5: refresh cost preview immediately
        refreshCostPreview();
        refreshLeadFleetAndRotateEffect(hp, gs.getPhase() == GamePhase.ACTION
            && gs.getActivePlayer() == hp && controller.isWaitingForHuman());
    }

    /** B5-0328 F4: drop the selection after a submit; buttons stay disabled. */
    private void clearSelection() {
        selectedCard = null;
        selectedTarget = null;
        selectedFleet = null;
        selectedAssistant = null;
        targetSelector.setSelectedIndex(-1);
        targetSelector.setEnabled(false);
        if (attackTargetSelector != null) {
            attackTargetSelector.removeAllItems();
            attackTargetSelector.addItem("(select target)");
            attackTargetSelector.setSelectedIndex(0);
            attackTargetSelector.setEnabled(false);
            attackTargetCards.clear();
            selectedAttackTarget = null;
        }
        if (leadFleetSelector != null) {
            leadFleetSelector.setSelectedIndex(-1);
            leadFleetSelector.setEnabled(false);
        }
        if (rotateEffectKindSelector != null) {
            rotateEffectKindSelector.setSelectedIndex(0);
            rotateEffectKindSelector.setEnabled(false);
        }
        // B5-0487: Censure opponent-fleet target picker reset
        if (censureTargetSelector != null) {
            censureTargetSelector.setSelectedIndex(-1);
            censureTargetSelector.setEnabled(false);
            censureTargetCards.clear();
            censureTargetOwnerNames.clear();
            selectedCensureTarget = null;
            selectedCensureTargetOwner = null;
            censurePlayButton.setEnabled(false);
        }
        // B5-0423: these three were populated by the submit that just resolved
        // but never reset, so the next turn started showing the previous card's
        // target / bid amount / contingency. Reset them to a harmless
        // non-actionable state. Each refresh path already re-derives the list
        // and re-enables on the next tick, so this is only a stale-display fix.
        if (warTargetSelector != null) {
            warTargetSelector.removeAllItems();
            warTargetSelector.addItem("(select target)");
            warTargetSelector.setEnabled(false);
        }
        if (mercenaryBidAmountSelector != null) {
            // Index 0, not -1: refreshMercenaryBid parses getSelectedItem()
            // with no null guard, and a -1 index makes it null.
            mercenaryBidAmountSelector.setSelectedIndex(0);
            mercenaryBidAmountSelector.setEnabled(false);
        }
        if (contingencySelector != null) {
            contingencySelector.removeAllItems();
            contingencySelector.addItem("(none)");
            contingencySelector.setEnabled(false);
        }
        // Drop the board highlight too, or the lime stroke outlives the
        // selection that produced it.
        boardPanel.clearSelection();
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
