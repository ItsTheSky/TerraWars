package net.itsthesky.terrawars.util;

public final class StringUtils {

    /**
     * Generates a text-based progress bar.
     *
     * @param currentValue The current progress value as integer
     * @param maxValue The maximum possible value as integer
     * @param maxChars The maximum number of characters in the progress bar
     * @param filledChar The character to use for the filled portion
     * @param emptyChar The character to use for the empty portion
     * @return A string representing the progress bar
     */
    public static String generateProgressBar(int currentValue, int maxValue, int maxChars,
                                             String filledChar, String emptyChar) {
        // Validate inputs
        if (currentValue < 0 || maxValue <= 0 || currentValue > maxValue || maxChars <= 0) {
            return "Invalid input parameters";
        }

        // Calculate the fill ratio and number of filled characters
        double ratio = (double) currentValue / maxValue;
        int filledCount = (int) Math.round(ratio * maxChars);

        return String.valueOf(filledChar).repeat(Math.max(0, filledCount)) +
                String.valueOf(emptyChar).repeat(Math.max(0, maxChars - filledCount));
    }

}
