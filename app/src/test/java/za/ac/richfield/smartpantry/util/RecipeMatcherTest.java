package za.ac.richfield.smartpantry.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.model.RecipeIngredient;

public final class RecipeMatcherTest {
    private final Recipe soup = new Recipe(1, "Tomato Soup", "Cook.", Arrays.asList(
            new RecipeIngredient(1, 1, "tomato", 500, "g"),
            new RecipeIngredient(2, 1, "water", 1, "l")
    ));

    @Test
    public void matchesAliasesAndEquivalentUnits() {
        assertTrue(RecipeMatcher.canMake(soup, Arrays.asList(
                new PantryItem(1, "Tomatoes", 0.5, "kg", null),
                new PantryItem(2, "Water", 1000, "ml", null)
        )));
    }

    @Test
    public void excludesRecipeWhenAnyIngredientIsMissing() {
        assertFalse(RecipeMatcher.canMake(soup, Collections.singletonList(
                new PantryItem(1, "Tomato", 500, "g", null)
        )));
    }

    @Test
    public void excludesRecipeWhenQuantityIsInsufficient() {
        assertFalse(RecipeMatcher.canMake(soup, Arrays.asList(
                new PantryItem(1, "Tomato", 499, "g", null),
                new PantryItem(2, "Water", 1000, "ml", null)
        )));
    }

    @Test
    public void sumsDuplicatePantryRows() {
        assertTrue(RecipeMatcher.canMake(soup, Arrays.asList(
                new PantryItem(1, "Tomato", 250, "g", null),
                new PantryItem(2, "Tomatoes", 250, "g", null),
                new PantryItem(3, "Water", 1, "l", null)
        )));
    }
}
