package za.ac.richfield.smartpantry.util;

import java.text.Normalizer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Produces stable comparison keys for user-entered ingredient names.
 * Known irregular forms are resolved before a conservative final-word plural rule is applied.
 */
public final class IngredientNormalizer {
    private static final Map<String, String> ALIASES;

    static {
        Map<String, String> aliases = new HashMap<>();
        aliases.put("tomatoes", "tomato");
        aliases.put("potatoes", "potato");
        aliases.put("chillies", "chilli");
        aliases.put("chilies", "chilli");
        aliases.put("bell peppers", "bell pepper");
        aliases.put("capsicums", "bell pepper");
        aliases.put("spring onions", "spring onion");
        aliases.put("scallions", "spring onion");
        aliases.put("garbanzo beans", "chickpea");
        aliases.put("chickpeas", "chickpea");
        aliases.put("eggs", "egg");
        aliases.put("cheeses", "cheese");
        aliases.put("mayonnaises", "mayonnaise");
        aliases.put("oatmeal", "oats");
        aliases.put("bread slices", "bread slice");
        aliases.put("garlic cloves", "garlic clove");
        ALIASES = Collections.unmodifiableMap(aliases);
    }

    private IngredientNormalizer() {
    }

    public static String normalize(String rawName) {
        if (rawName == null) {
            return "";
        }

        String value = Normalizer.normalize(rawName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replace('&', ' ')
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        value = value.replaceAll("^(fresh|ripe|large|small|medium)\\s+", "");
        if (ALIASES.containsKey(value)) {
            return ALIASES.get(value);
        }

        String[] words = value.split(" ");
        if (words.length == 0) {
            return value;
        }
        words[words.length - 1] = singularize(words[words.length - 1]);
        String normalized = String.join(" ", words).trim();
        return ALIASES.getOrDefault(normalized, normalized);
    }

    private static String singularize(String word) {
        if (word.isEmpty() || word.equals("oats") || word.equals("couscous")) {
            return word;
        }
        if (word.endsWith("ies") && word.length() > 3) {
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("oes") && word.length() > 3) {
            return word.substring(0, word.length() - 2);
        }
        if ((word.endsWith("ches") || word.endsWith("shes") || word.endsWith("xes")
                || word.endsWith("ses")) && word.length() > 4) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("s") && !word.endsWith("ss") && !word.endsWith("us")
                && word.length() > 2) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }
}
