package za.ac.richfield.smartpantry.model;

import java.util.Objects;

public final class PantryItem {
    private final long id;
    private final String name;
    private final double quantity;
    private final String unit;
    private final String expiryDate;

    public PantryItem(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name");
        this.quantity = quantity;
        this.unit = Objects.requireNonNull(unit, "unit");
        this.expiryDate = expiryDate;
    }

    public long getId() {
        return id;
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

    public String getExpiryDate() {
        return expiryDate;
    }
}
