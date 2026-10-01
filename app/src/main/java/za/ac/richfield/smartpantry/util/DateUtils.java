package za.ac.richfield.smartpantry.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class DateUtils {
    public enum ExpiryStatus {
        NONE,
        NORMAL,
        SOON,
        EXPIRED
    }

    private static final String ISO_DATE_PATTERN = "yyyy-MM-dd";

    private DateUtils() {
    }

    public static boolean isValidIsoDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        return parseIsoDate(value) != null;
    }

    public static ExpiryStatus getExpiryStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ExpiryStatus.NONE;
        }
        Date expiry = parseIsoDate(value);
        if (expiry == null) {
            return ExpiryStatus.NONE;
        }

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        long difference = expiry.getTime() - today.getTimeInMillis();
        long days = TimeUnit.MILLISECONDS.toDays(difference);
        if (days < 0) {
            return ExpiryStatus.EXPIRED;
        }
        if (days <= 3) {
            return ExpiryStatus.SOON;
        }
        return ExpiryStatus.NORMAL;
    }

    public static String formatIsoDate(int year, int zeroBasedMonth, int day) {
        return String.format(Locale.ROOT, "%04d-%02d-%02d", year, zeroBasedMonth + 1, day);
    }

    private static Date parseIsoDate(String value) {
        SimpleDateFormat format = new SimpleDateFormat(ISO_DATE_PATTERN, Locale.ROOT);
        format.setLenient(false);
        try {
            return format.parse(value.trim());
        } catch (ParseException exception) {
            return null;
        }
    }
}
