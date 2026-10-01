package za.ac.richfield.smartpantry.model;

import java.util.Objects;

public final class RecipeIngredient {
    private final long id;
    private final long recipeId;
    private final String name;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(long id, long recipeId, String name, double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.name = Objects.requireNonNull(name, "name");
        this.quantity = quantity;
        this.unit = Objects.requireNonNull(unit, "unit");
    }

    public long getId() {
        return id;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }
}
