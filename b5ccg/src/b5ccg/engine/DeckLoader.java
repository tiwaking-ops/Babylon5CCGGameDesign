package b5ccg.engine;

import b5ccg.model.*;
import b5ccg.model.enums.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Minimal JSON parser for card data — no external libraries.
 * Parses the flat JSON array produced in premiere.json / deluxe.json.
 */
public class DeckLoader {

    public static List<Card> loadFromResource(String resourcePath) throws IOException {
        URL url = DeckLoader.class.getResource(resourcePath);
        if (url == null) throw new FileNotFoundException("Resource not found: " + resourcePath);
        try (InputStream is = url.openStream()) {
            String json = readAll(is);
            return parseCards(json);
        }
    }

    public static List<Card> loadBothSets() throws IOException {
        List<Card> all = new ArrayList<>();
        all.addAll(loadFromResource("/cards/premiere.json"));
        all.addAll(loadFromResource("/cards/deluxe.json"));
        return all;
    }

    // ── Internal JSON parsing ─────────────────────────────────────────────────

    private static String readAll(InputStream is) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int n;
        while ((n = is.read(chunk)) != -1) buf.write(chunk, 0, n);
        return buf.toString(StandardCharsets.UTF_8.name());
    }

    static List<Card> parseCards(String json) {
        List<Card> cards = new ArrayList<>();
        List<Map<String, String>> objects = splitObjects(json);
        for (Map<String, String> obj : objects) {
            try {
                cards.add(buildCard(obj));
            } catch (Exception e) {
                System.err.println("Skipping card, parse error: " + e.getMessage()
                    + " | data=" + obj.get("id"));
            }
        }
        return cards;
    }

    /** Split the top-level JSON array into individual object maps. */
    private static List<Map<String, String>> splitObjects(String json) {
        List<Map<String, String>> result = new ArrayList<>();
        int depth = 0;
        int start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    result.add(parseObject(json.substring(start, i + 1)));
                    start = -1;
                }
            }
        }
        return result;
    }

    /** Parse a single JSON object { "key":"value", ... } into a Map. */
    private static Map<String, String> parseObject(String obj) {
        Map<String, String> map = new LinkedHashMap<>();
        // Strip outer braces
        obj = obj.trim();
        if (obj.startsWith("{")) obj = obj.substring(1);
        if (obj.endsWith("}"))   obj = obj.substring(0, obj.length() - 1);

        // Simple tokenisation — handles string and primitive values.
        int i = 0;
        while (i < obj.length()) {
            // Find key
            int ks = obj.indexOf('"', i);
            if (ks < 0) break;
            int ke = obj.indexOf('"', ks + 1);
            if (ke < 0) break;
            String key = obj.substring(ks + 1, ke);
            i = ke + 1;

            // Find colon
            int colon = obj.indexOf(':', i);
            if (colon < 0) break;
            i = colon + 1;

            // Skip whitespace
            while (i < obj.length() && Character.isWhitespace(obj.charAt(i))) i++;

            String value;
            if (i < obj.length() && obj.charAt(i) == '"') {
                // String value — handle escaped quotes
                StringBuilder sb = new StringBuilder();
                i++; // skip opening quote
                while (i < obj.length()) {
                    char c = obj.charAt(i);
                    if (c == '\\' && i + 1 < obj.length()) {
                        char next = obj.charAt(i + 1);
                        if (next == '"') { sb.append('"'); i += 2; continue; }
                        if (next == '\\') { sb.append('\\'); i += 2; continue; }
                        if (next == 'n') { sb.append('\n'); i += 2; continue; }
                        sb.append(c); i++; continue;
                    }
                    if (c == '"') { i++; break; }
                    sb.append(c); i++;
                }
                value = sb.toString();
            } else {
                // Primitive (number/boolean)
                int end = i;
                while (end < obj.length() && obj.charAt(end) != ',' && obj.charAt(end) != '}') end++;
                value = obj.substring(i, end).trim();
                i = end;
            }
            map.put(key, value);
        }
        return map;
    }

    private static Card buildCard(Map<String, String> m) {
        String id       = req(m, "id");
        String title    = req(m, "title");
        String typeStr  = req(m, "type");
        String subtype  = m.getOrDefault("subtype", "");
        String rarStr   = m.getOrDefault("rarity", "COMMON");
        String facStr   = m.getOrDefault("faction", "ANY");
        String setStr   = m.getOrDefault("set", "PREMIERE");
        String imgKey   = m.getOrDefault("imageKey", id);
        String text     = m.getOrDefault("text", "");

        CardType type    = CardType.valueOf(typeStr.toUpperCase());
        Rarity   rarity  = parseRarity(rarStr);
        Faction  faction = parseFaction(facStr);
        CardSet  cardSet = CardSet.valueOf(setStr.toUpperCase());

        switch (type) {
            case CHARACTER: {
                int dip  = intVal(m, "diplomacy",  0);
                int intr = intVal(m, "intrigue",   0);
                int psi  = intVal(m, "psi",        0);
                int lead = intVal(m, "leadership", 0);
                boolean isAmb = boolVal(m, "isAmbassador", false);
                return new CharacterCard(id, title, subtype, rarity, faction, cardSet,
                                         imgKey, text, dip, intr, psi, lead, isAmb);
            }
            case FLEET: {
                int mil = intVal(m, "military", 1);
                return new FleetCard(id, title, subtype, rarity, faction, cardSet,
                                     imgKey, text, mil);
            }
            case CONFLICT: {
                ConflictType ct  = ConflictType.valueOf(
                    m.getOrDefault("conflictType", "DIPLOMACY").toUpperCase());
                int reward = intVal(m, "influenceReward", 1);
                return new ConflictCard(id, title, subtype, rarity, faction, cardSet,
                                        imgKey, text, ct, reward);
            }
            case AGENDA: {
                boolean major  = boolVal(m, "isMajorAgenda", false);
                String  winKey = m.getOrDefault("winCondition", "INFLUENCE_20");
                return new AgendaCard(id, title, subtype, rarity, faction, cardSet,
                                      imgKey, text, major, winKey);
            }
            case AFTERMATH: {
                String trigger = m.getOrDefault("triggerCondition", "ANY");
                return new AftermathCard(id, title, subtype, rarity, faction, cardSet,
                                         imgKey, text, trigger);
            }
            case EVENT:
                return new EventCard(id, title, subtype, rarity, faction, cardSet, imgKey, text);
            case ENHANCEMENT: {
                int dip  = intVal(m, "diplomacyBonus",  0);
                int intr = intVal(m, "intrigueBonus",   0);
                int psi  = intVal(m, "psiBonus",        0);
                int mil  = intVal(m, "militaryBonus",   0);
                int lead = intVal(m, "leadershipBonus", 0);
                return new EnhancementCard(id, title, subtype, rarity, faction, cardSet,
                                           imgKey, text, dip, intr, psi, mil, lead);
            }
            case GROUP:
                return new GroupCard(id, title, subtype, rarity, faction, cardSet, imgKey, text);
            case LOCATION: {
                int income = intVal(m, "influencePerRound", 1);
                return new LocationCard(id, title, subtype, rarity, faction, cardSet,
                                        imgKey, text, income);
            }
            default:
                throw new IllegalArgumentException("Unknown card type: " + type);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String req(Map<String, String> m, String key) {
        String v = m.get(key);
        if (v == null) throw new IllegalArgumentException("Missing required field: " + key);
        return v;
    }

    private static int intVal(Map<String, String> m, String key, int def) {
        try { return Integer.parseInt(m.getOrDefault(key, String.valueOf(def)).trim()); }
        catch (NumberFormatException e) { return def; }
    }

    private static boolean boolVal(Map<String, String> m, String key, boolean def) {
        String v = m.get(key);
        if (v == null) return def;
        return "true".equalsIgnoreCase(v.trim());
    }

    private static Rarity parseRarity(String s) {
        try { return Rarity.valueOf(s.toUpperCase().replace("-", "_")); }
        catch (IllegalArgumentException e) { return Rarity.COMMON; }
    }

    private static Faction parseFaction(String s) {
        try { return Faction.valueOf(s.toUpperCase().replace("-", "_")); }
        catch (IllegalArgumentException e) { return Faction.ANY; }
    }
}
