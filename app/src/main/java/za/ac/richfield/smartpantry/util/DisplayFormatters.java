package za.ac.richfield.smartpantry.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class DisplayFormatters {
    private DisplayFormatters() {
    }

    public static String quantity(double value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
        return new DecimalFormat("0.##", symbols).format(value);
    }
}
