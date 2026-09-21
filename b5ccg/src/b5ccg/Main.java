package b5ccg;

import b5ccg.ai.AIPlayer;
import b5ccg.engine.*;
import b5ccg.model.*;
import b5ccg.model.enums.*;
import b5ccg.ui.MainWindow;
import javax.swing.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}

                try {
                    startGame();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                        "Fatal error starting game:\n" + e.getMessage(),
                        "B5 CCG Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });
    }

    private static void startGame() throws Exception {
        // ── Load all cards ────────────────────────────────────────────────────
        List<Card> allCards;
        try {
            allCards = DeckLoader.loadBothSets();
            System.out.println("Loaded " + allCards.size() + " cards.");
        } catch (Exception e) {
            System.err.println("Card load failed, using minimal test set: " + e.getMessage());
            allCards = buildMinimalTestSet();
        }

        // ── Create players ────────────────────────────────────────────────────
        Faction humanFaction = chooseFaction();
        List<Faction> aiFactions = otherFactions(humanFaction);

        Player human  = new Player("You",       humanFaction,          true);
        Player ai1    = new Player("Delenn",    aiFactions.get(0),     false);
        Player ai2    = new Player("G'Kar",     aiFactions.get(1),     false);
        Player ai3    = new Player("Londo",     aiFactions.get(2),     false);

        List<Player> players = Arrays.asList(human, ai1, ai2, ai3);

        // ── Build and deal faction decks ──────────────────────────────────────
        for (Player p : players) {
            List<Card> factionCards = buildFactionDeck(allCards, p.getFaction());
            Deck deck = new Deck(factionCards);
            p.setDeck(deck);
            p.drawCards(4); // initial hand
        }

        // ── AI players ────────────────────────────────────────────────────────
        List<AIPlayer> aiPlayers = Arrays.asList(
            new AIPlayer(ai1, AIDifficulty.MEDIUM),
            new AIPlayer(ai2, AIDifficulty.HARD),
            new AIPlayer(ai3, AIDifficulty.EASY)
        );

        // ── Game state & controller ───────────────────────────────────────────
        GameState state = new GameState(players);
        final MainWindow[] windowHolder = new MainWindow[1];

        GameController controller = new GameController(state, aiPlayers,
            new GameStateCallback() {
                @Override public void accept(GameState gs) {
                    if (windowHolder[0] != null) windowHolder[0].onStateUpdate(gs);
                }
            });

        final GameController fController = controller;
        MainWindow window = new MainWindow(controller);
        windowHolder[0] = window;
        window.setVisible(true);

        // ── Run game loop on background thread ───────────────────────────────
        Thread gameThread = new Thread(new Runnable() {
            @Override public void run() { fController.runGame(); }
        }, "GameLoop");
        gameThread.setDaemon(true);
        gameThread.start();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Faction chooseFaction() {
        Object[] options = { "Human", "Minbari", "Centauri", "Narn" };
        int choice = JOptionPane.showOptionDialog(null,
            "Choose your faction:", "Babylon 5 CCG — Choose Faction",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);
        switch (choice) {
            case 1: return Faction.MINBARI;
            case 2: return Faction.CENTAURI;
            case 3: return Faction.NARN;
            default: return Faction.HUMAN;
        }
    }

    private static List<Faction> otherFactions(Faction chosen) {
        List<Faction> all = new ArrayList<Faction>(
            Arrays.asList(Faction.HUMAN, Faction.MINBARI, Faction.CENTAURI, Faction.NARN));
        all.remove(chosen);
        return all;
    }

    private static List<Card> buildFactionDeck(List<Card> all, Faction faction) {
        List<Card> deck = new ArrayList<Card>();
        // 1. Faction-specific cards first
        for (Card c : all) {
            if (c.getFaction() == faction && deck.size() < 30) deck.add(c);
        }
        // 2. Neutral and ANY faction fillers
        for (Card c : all) {
            if (deck.size() >= 60) break;
            Faction f = c.getFaction();
            if (f == Faction.ANY || f == Faction.NEUTRAL || f == Faction.NON_ALIGNED) {
                deck.add(c);
            }
        }
        // 3. Pad to 60 if needed
        Collections.shuffle(deck);
        while (deck.size() < 60 && !all.isEmpty()) {
            deck.add(all.get(new Random().nextInt(all.size())));
        }
        return deck.subList(0, Math.min(60, deck.size()));
    }

    /** Minimal fallback card set used if JSON files are not on the classpath. */
    private static List<Card> buildMinimalTestSet() {
        List<Card> cards = new ArrayList<Card>();
        cards.add(new CharacterCard("test_sinclair","Jeffrey Sinclair","CHARACTER_HUMAN",
            Rarity.FIXED, Faction.HUMAN, CardSet.PREMIERE,"jeffrey_sinclair",
            "Human Ambassador.",5,3,0,4,true));
        cards.add(new CharacterCard("test_gkar","G'Kar","CHARACTER_NARN",
            Rarity.FIXED, Faction.NARN, CardSet.PREMIERE,"gkar",
            "Narn Ambassador.",4,5,0,3,true));
        cards.add(new CharacterCard("test_delenn","Delenn","CHARACTER_MINBARI",
            Rarity.FIXED, Faction.MINBARI, CardSet.PREMIERE,"delenn",
            "Minbari Ambassador.",7,3,2,5,true));
        cards.add(new CharacterCard("test_londo","Londo Mollari","CHARACTER_CENTAURI",
            Rarity.FIXED, Faction.CENTAURI, CardSet.PREMIERE,"londo_mollari",
            "Centauri Ambassador.",5,6,0,4,true));
        for (int i = 0; i < 15; i++) {
            cards.add(new ConflictCard("test_dip_" + i, "Campaign for Support",
                "CONFLICT_DIPLOMACY", Rarity.COMMON, Faction.ANY, CardSet.PREMIERE,
                "campaign_for_support", "Diplomacy conflict. Winner gains 2 Influence.",
                ConflictType.DIPLOMACY, 2));
            cards.add(new ConflictCard("test_mil_" + i, "Border Raid",
                "CONFLICT_MILITARY", Rarity.FIXED, Faction.ANY, CardSet.PREMIERE,
                "border_raid", "Military conflict. Winner gains 2 Influence.",
                ConflictType.MILITARY, 2));
            cards.add(new FleetCard("test_fleet_" + i, "Picket Fleet",
                "FLEET_HUMAN", Rarity.FIXED, Faction.ANY, CardSet.PREMIERE,
                "picket_fleet_human", "Basic patrol fleet.", 3));
            cards.add(new EventCard("test_evt_" + i, "Short Term Goals",
                "EVENT", Rarity.FIXED, Faction.ANY, CardSet.PREMIERE,
                "short_term_goals", "Gain 1 Influence immediately."));
        }
        return cards;
    }
}
