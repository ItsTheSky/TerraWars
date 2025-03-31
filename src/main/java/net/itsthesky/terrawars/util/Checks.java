package net.itsthesky.terrawars.util;

public class Checks {

    public static <T> T notNull(T reference, String message) {
        if (reference == null) {
            throw new NullPointerException(message);
        }
        return reference;
    }

    public static <T> T notNull(T reference) {
        return notNull(reference, "Reference cannot be null");
    }

    public static void notEmpty(String reference, String message) {
        if (reference == null || reference.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(String reference) {
        notEmpty(reference, "String cannot be null or empty");
    }

    public static void notBlank(String reference, String message) {
        if (reference == null || reference.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notBlank(String reference) {
        notBlank(reference, "String cannot be null or blank");
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkNullOrEmpty(String reference, String message) {
        if (reference != null && !reference.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkNullOrEmpty(String reference) {
        checkNullOrEmpty(reference, "String cannot be null or empty");
    }
}
