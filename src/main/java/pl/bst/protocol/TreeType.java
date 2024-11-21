package pl.bst.protocol;

import java.util.Locale;
public enum TreeType {
    INTEGER, DOUBLE, STRING;
    public static TreeType parse(String value) {
        return valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
    public static Double finiteDouble(String value) {
        double parsed = Double.parseDouble(value);
        if (!Double.isFinite(parsed)) throw new IllegalArgumentException("Expected a finite double");
        return parsed;
    }
}
