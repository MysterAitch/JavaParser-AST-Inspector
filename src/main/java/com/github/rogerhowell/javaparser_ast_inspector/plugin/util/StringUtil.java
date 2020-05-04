package com.github.rogerhowell.javaparser_ast_inspector.plugin.util;

public final class StringUtil {



    public static String padEnd(String input, int desiredMinimumLength) {
        if(input.length() > desiredMinimumLength) {
            return input;
        }

        while(input.length() < desiredMinimumLength) {
            input = input + " ";
        }

        return input;
    }
    public static String padStart(String input, int desiredMinimumLength) {
        if(input.length() > desiredMinimumLength) {
            return input;
        }

        while(input.length() < desiredMinimumLength) {
            input = " " + input;
        }

        return input;
    }

}
