package com.github.rogerhowell.javaparser_ast_inspector.plugin.util;

import org.jetbrains.annotations.NotNull;

public final class StringUtil {


    private StringUtil() {
        // Empty private constructor, to prevent instantiation.
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

    @SuppressWarnings({"HardcodedFileSeparator"})
    @NotNull
    public static String escapeTab(String string) {
        return string.replace("\t", "\\t");
    }

    @SuppressWarnings({"HardcodedLineSeparator", "HardcodedFileSeparator"})
    @NotNull
    public static String escapeNewlines(String string) {
        return string
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\r\n", "\\r\\n");
    }

    @NotNull
    public static String escapeWhitespace(String string) {
        return escapeTab(escapeNewlines(string));
    }
}
