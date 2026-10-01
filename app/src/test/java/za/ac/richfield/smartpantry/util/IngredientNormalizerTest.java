package za.ac.richfield.smartpantry.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class IngredientNormalizerTest {
    @Test
    public void normalizesPluralAndDescriptiveNames() {
        assertEquals("tomato", IngredientNormalizer.normalize("Fresh Tomatoes"));
        assertEquals("potato", IngredientNormalizer.normalize("potatoes"));
        assertEquals("bread slice", IngredientNormalizer.normalize("Bread slices"));
        assertEquals("cheese", IngredientNormalizer.normalize("cheeses"));
        assertEquals("mayonnaise", IngredientNormalizer.normalize("mayonnaises"));
    }

    @Test
    public void resolvesConfiguredAliases() {
        assertEquals("bell pepper", IngredientNormalizer.normalize("Capsicums"));
        assertEquals("chickpea", IngredientNormalizer.normalize("Garbanzo beans"));
    }
}
