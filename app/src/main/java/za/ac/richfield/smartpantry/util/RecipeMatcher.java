package za.ac.richfield.smartpantry.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.model.RecipeIngredient;

/**
 * Applies the assignment's strict rule: every requirement must be present at a sufficient quantity.
 */
public final class RecipeMatcher {
    private static final double EPSILON = 0.0001;

    private RecipeMatcher() {
    }

    public static boolean canMake(Recipe recipe, List<PantryItem> pantryItems) {
        Map<String, Double> available = buildAvailableQuantityMap(pantryItems);

        // Fail fast on the first missing or insufficient ingredient; partial matches are never returned.
        for (RecipeIngredient required : recipe.getIngredients()) {
            String key = matchingKey(required.getName(), required.getUnit());
            double availableQuantity = available.getOrDefault(key, 0.0);
            double requiredQuantity = UnitConverter.toBase(required.getQuantity(), required.getUnit());
            if (availableQuantity + EPSILON < requiredQuantity) {
                return false;
            }
        }
        return true;
    }

    private static Map<String, Double> buildAvailableQuantityMap(List<PantryItem> pantryItems) {
        Map<String, Double> quantities = new HashMap<>();
        for (PantryItem item : pantryItems) {
            String key = matchingKey(item.getName(), item.getUnit());
            double baseQuantity = UnitConverter.toBase(item.getQuantity(), item.getUnit());
            quantities.merge(key, baseQuantity, Double::sum);
        }
        return quantities;
    }

    private static String matchingKey(String ingredientName, String unit) {
        UnitConverter.Dimension dimension = UnitConverter.dimensionOf(unit);
        String unitGroup = dimension == UnitConverter.Dimension.UNKNOWN
                ? UnitConverter.normalizeUnit(unit)
                : dimension.name();
        return IngredientNormalizer.normalize(ingredientName) + "|" + unitGroup;
    }
}
