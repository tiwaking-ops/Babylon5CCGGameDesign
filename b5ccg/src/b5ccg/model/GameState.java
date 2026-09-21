package b5ccg.model;

import b5ccg.model.enums.*;
import java.util.*;

public class GameState {
    private final List<Player> players;
    private       int          currentPlayerIndex = 0;
    private       int          roundNumber        = 1;
    private       GamePhase    phase              = GamePhase.SETUP;
    private       Conflict     activeConflict;
    private       Player       winner;

    private final List<String> log = new ArrayList<>();

    public GameState(List<Player> players) {
        this.players = new ArrayList<>(players);
    }

    // ── Players ───────────────────────────────────────────────────────────────
    public List<Player> getPlayers()       { return Collections.unmodifiableList(players); }
    public Player getActivePlayer()        { return players.get(currentPlayerIndex); }
    public Player getHumanPlayer()         {
        return players.stream().filter(Player::isHuman).findFirst().orElse(players.get(0));
    }

    public void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    // ── Round ─────────────────────────────────────────────────────────────────
    public int       getRoundNumber() { return roundNumber; }
    public void      advanceRound()   { roundNumber++; currentPlayerIndex = 0; }

    // ── Phase ─────────────────────────────────────────────────────────────────
    public GamePhase getPhase()          { return phase; }
    public void      setPhase(GamePhase p) { phase = p; log("Phase → " + p); }

    // ── Conflict ──────────────────────────────────────────────────────────────
    public Conflict getActiveConflict()              { return activeConflict; }
    public void     setActiveConflict(Conflict c)    { activeConflict = c; }
    public void     clearActiveConflict()            { activeConflict = null; }

    // ── Victory ───────────────────────────────────────────────────────────────
    public Player  getWinner()            { return winner; }
    public boolean isGameOver()           { return winner != null; }
    public void    setWinner(Player p)    { winner = p; log("WINNER: " + p.getName()); }

    // ── Log ───────────────────────────────────────────────────────────────────
    public void log(String msg)          { log.add("[R" + roundNumber + "] " + msg); }
    public List<String> getLog()         { return Collections.unmodifiableList(log); }
    public String getLastLogEntry()      { return log.isEmpty() ? "" : log.get(log.size() - 1); }
}
