package b5ccg.model.enums;

/** B5-0366 (B5-0357 proposal §3.1): the stats a bonus can modify.
 *  INFLUENCE is deliberately absent — D9/B5-0342 owns the
 *  rating-vs-applied-influence pool, it is not a combat stat here.
 */
public enum StatKey {
    DIPLOMACY,
    INTRIGUE,
    PSI,
    LEADERSHIP,
    MILITARY
}
