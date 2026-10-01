import java.util.Arrays;
import java.util.Collections;

import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.model.RecipeIngredient;
import za.ac.richfield.smartpantry.util.IngredientNormalizer;
import za.ac.richfield.smartpantry.util.RecipeMatcher;

public final class RecipeMatcherSelfTest {
    public static void main(String[] args) {
        assertEquals("tomato", IngredientNormalizer.normalize("Fresh Tomatoes"));
        assertEquals("potato", IngredientNormalizer.normalize("potatoes"));
        assertEquals("bread slice", IngredientNormalizer.normalize("Bread slices"));
        assertEquals("cheese", IngredientNormalizer.normalize("cheeses"));
        assertEquals("mayonnaise", IngredientNormalizer.normalize("mayonnaises"));
        assertEquals("bell pepper", IngredientNormalizer.normalize("Capsicums"));
        assertEquals("chickpea", IngredientNormalizer.normalize("Garbanzo beans"));

        Recipe soup = new Recipe(1, "Tomato Soup", "Cook the ingredients.", Arrays.asList(
                new RecipeIngredient(1, 1, "tomato", 500, "g"),
                new RecipeIngredient(2, 1, "water", 1, "l")
        ));

        assertTrue(RecipeMatcher.canMake(soup, Arrays.asList(
                new PantryItem(1, "Tomatoes", 0.5, "kg", null),
                new PantryItem(2, "Water", 1000, "ml", null)
        )), "Equivalent names and units should match");

        assertFalse(RecipeMatcher.canMake(soup, Collections.singletonList(
                new PantryItem(1, "Tomatoes", 0.5, "kg", null)
        )), "A missing ingredient must exclude the recipe");

        assertFalse(RecipeMatcher.canMake(soup, Arrays.asList(
                new PantryItem(1, "Tomato", 499, "g", null),
                new PantryItem(2, "Water", 1000, "ml", null)
        )), "Insufficient quantity must exclude the recipe");

        Recipe dressing = new Recipe(2, "Simple Dressing", "Mix.", Collections.singletonList(
                new RecipeIngredient(3, 2, "oil", 30, "ml")
        ));
        assertTrue(RecipeMatcher.canMake(dressing, Collections.singletonList(
                new PantryItem(3, "Oil", 2, "tbsp", null)
        )), "Common volume units should convert to millilitres");

        System.out.println("All RecipeMatcher self-tests passed.");
    }

    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but found " + actual);
        }
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean value, String message) {
        assertTrue(!value, message);
    }
}
