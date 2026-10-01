package za.ac.richfield.smartpantry.util;

import java.util.Locale;

/**
 * Normalises units and converts quantities only within compatible measurement dimensions.
 */
public final class UnitConverter {
    public enum Dimension {
        MASS,
        VOLUME,
        COUNT,
        UNKNOWN
    }

    private UnitConverter() {
    }

    public static String normalizeUnit(String rawUnit) {
        if (rawUnit == null) {
            return "";
        }
        String unit = rawUnit.toLowerCase(Locale.ROOT).trim().replace(".", "");
        switch (unit) {
            case "gram":
            case "grams":
                return "g";
            case "milligram":
            case "milligrams":
            case "mgs":
                return "mg";
            case "kilogram":
            case "kilograms":
            case "kgs":
                return "kg";
            case "millilitre":
            case "millilitres":
            case "milliliter":
            case "milliliters":
                return "ml";
            case "litre":
            case "litres":
            case "liter":
            case "liters":
                return "l";
            case "teaspoon":
            case "teaspoons":
                return "tsp";
            case "tablespoon":
            case "tablespoons":
                return "tbsp";
            case "cups":
                return "cup";
            case "each":
            case "piece":
            case "pieces":
            case "unit":
            case "units":
            case "slice":
            case "slices":
            case "clove":
            case "cloves":
                return "count";
            default:
                return unit;
        }
    }

    public static Dimension dimensionOf(String rawUnit) {
        String unit = normalizeUnit(rawUnit);
        if (unit.equals("mg") || unit.equals("g") || unit.equals("kg")) {
            return Dimension.MASS;
        }
        if (unit.equals("ml") || unit.equals("l") || unit.equals("tsp")
                || unit.equals("tbsp") || unit.equals("cup")) {
            return Dimension.VOLUME;
        }
        if (unit.equals("count")) {
            return Dimension.COUNT;
        }
        return Dimension.UNKNOWN;
    }

    public static double toBase(double quantity, String rawUnit) {
        String unit = normalizeUnit(rawUnit);
        switch (unit) {
            case "mg":
                return quantity * 0.001;
            case "kg":
            case "l":
                return quantity * 1000.0;
            case "tsp":
                return quantity * 5.0;
            case "tbsp":
                return quantity * 15.0;
            case "cup":
                return quantity * 250.0;
            default:
                return quantity;
        }
    }

    public static boolean areCompatible(String firstUnit, String secondUnit) {
        Dimension first = dimensionOf(firstUnit);
        Dimension second = dimensionOf(secondUnit);
        if (first == Dimension.UNKNOWN || second == Dimension.UNKNOWN) {
            return normalizeUnit(firstUnit).equals(normalizeUnit(secondUnit));
        }
        return first == second;
    }
}
