package b5ccg.model;

/**
 * B5-0340 — the Babylon 5 station as a non-player influence force
 * (rulebook :153 "Influence": "As Babylon 5's influence grows, the galaxy
 * becomes a safer place"; B5-0354 research report).
 *
 * The rulebook prints no starting rating and no drift schedule, and no card
 * in the current data moves station influence (B5-0354: grep "station" = 0
 * in resources/). The start value is therefore a recorded design decision
 * (DECISIONS B5-0340): start 0, no scheduled drift — Standard Victory
 * condition 2 (rulebook :176) stays inert until a real station-influence
 * source exists. The named constants keep the decision greppable.
 */
public class Babylon5Station {

    /** Design decision (B5-0354 research, DECISIONS B5-0340): the rulebook
     *  names no start value; 0 is the most conservative choice. */
    public static final int STATION_START_INFLUENCE = 0;

    /** Standard Victory condition 2 threshold (rulebook :176). */
    public static final int CONDITION_2_THRESHOLD = 20;

    private int influence = STATION_START_INFLUENCE;

    public int  getInfluence()       { return influence; }
    public void gainInfluence(int n) { influence += n; }
    public void loseInfluence(int n) { influence = Math.max(0, influence - n); }
}
