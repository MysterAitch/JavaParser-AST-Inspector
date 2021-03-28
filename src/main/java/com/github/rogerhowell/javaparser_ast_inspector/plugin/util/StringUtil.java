package com.github.rogerhowell.javaparser_ast_inspector.plugin.util;

public final class StringUtil {


    private StringUtil() {
    }

    /**
     * @param input                The text to have padding applied.
     * @param desiredMinimumLength The minimum length of the output.
     * @return When the input length is > desired minimum length, the input.
     * Otherwise the input padded with spaces at the END, up to the desiredMinimumLength
     */
    public static String padEnd(String input, int desiredMinimumLength) {
        if (input.length() > desiredMinimumLength) {
            return input;
        }

        StringBuilder inputBuilder = new StringBuilder(input);
        while (inputBuilder.length() < desiredMinimumLength) {
            inputBuilder.append(" ");
        }

        return inputBuilder.toString();
    }

    /**
     * @param input                The text to have padding applied.
     * @param desiredMinimumLength The minimum length of the output.
     * @return When the input length is > desired minimum length, the input.
     * Otherwise the input padded with spaces at the START, up to the desiredMinimumLength
     */
    public static String padStart(String input, int desiredMinimumLength) {
        if (input.length() > desiredMinimumLength) {
            return input;
        }

        StringBuilder inputBuilder = new StringBuilder(input);
        while (inputBuilder.length() < desiredMinimumLength) {
            inputBuilder.insert(0, " ");
        }

        return inputBuilder.toString();
    }

}
