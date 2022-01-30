package com.github.rogerhowell.javaparser_ast_inspector.plugin.util;

import com.github.javaparser.ParserConfiguration;
import com.intellij.pom.java.LanguageLevel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class LanguageLevelUtilTest {


    private static Stream<Arguments> mappingNoPrefix() {
        return Stream.of(
                Arguments.of("X", ParserConfiguration.LanguageLevel.BLEEDING_EDGE),
                Arguments.of("0", ParserConfiguration.LanguageLevel.JAVA_1_0),
                Arguments.of("1", ParserConfiguration.LanguageLevel.JAVA_1_1),
                Arguments.of("2", ParserConfiguration.LanguageLevel.JAVA_1_2),
                Arguments.of("3", ParserConfiguration.LanguageLevel.JAVA_1_3),
                Arguments.of("4", ParserConfiguration.LanguageLevel.JAVA_1_4),
                Arguments.of("5", ParserConfiguration.LanguageLevel.JAVA_5),
                Arguments.of("6", ParserConfiguration.LanguageLevel.JAVA_6),
                Arguments.of("7", ParserConfiguration.LanguageLevel.JAVA_7),
                Arguments.of("8", ParserConfiguration.LanguageLevel.JAVA_8),
                Arguments.of("9", ParserConfiguration.LanguageLevel.JAVA_9),
                Arguments.of("10", ParserConfiguration.LanguageLevel.JAVA_10),
                Arguments.of("11", ParserConfiguration.LanguageLevel.JAVA_11),
                Arguments.of("12", ParserConfiguration.LanguageLevel.JAVA_12),
                Arguments.of("13", ParserConfiguration.LanguageLevel.JAVA_13),
                Arguments.of("14", ParserConfiguration.LanguageLevel.JAVA_14),
                Arguments.of("14_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_14_PREVIEW),
                Arguments.of("15", ParserConfiguration.LanguageLevel.JAVA_15),
                Arguments.of("15_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_15_PREVIEW),
                Arguments.of("16", ParserConfiguration.LanguageLevel.JAVA_16),
                Arguments.of("16_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_16_PREVIEW),
                Arguments.of("17", ParserConfiguration.LanguageLevel.JAVA_17),
                Arguments.of("17_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_17_PREVIEW)
        );
    }


    private static Stream<Arguments> mappingJava_1() {
        return Stream.of(
                Arguments.of("JAVA_1_X", ParserConfiguration.LanguageLevel.BLEEDING_EDGE),
                Arguments.of("JAVA_1_0", ParserConfiguration.LanguageLevel.JAVA_1_0),
                Arguments.of("JAVA_1_1", ParserConfiguration.LanguageLevel.JAVA_1_1),
                Arguments.of("JAVA_1_2", ParserConfiguration.LanguageLevel.JAVA_1_2),
                Arguments.of("JAVA_1_3", ParserConfiguration.LanguageLevel.JAVA_1_3),
                Arguments.of("JAVA_1_4", ParserConfiguration.LanguageLevel.JAVA_1_4),
                Arguments.of("JAVA_1_5", ParserConfiguration.LanguageLevel.JAVA_5),
                Arguments.of("JAVA_1_6", ParserConfiguration.LanguageLevel.JAVA_6),
                Arguments.of("JAVA_1_7", ParserConfiguration.LanguageLevel.JAVA_7),
                Arguments.of("JAVA_1_8", ParserConfiguration.LanguageLevel.JAVA_8),
                Arguments.of("JAVA_1_9", ParserConfiguration.LanguageLevel.JAVA_9),
                Arguments.of("JAVA_1_10", ParserConfiguration.LanguageLevel.JAVA_10),
                Arguments.of("JAVA_1_11", ParserConfiguration.LanguageLevel.JAVA_11),
                Arguments.of("JAVA_1_12", ParserConfiguration.LanguageLevel.JAVA_12),
                Arguments.of("JAVA_1_13", ParserConfiguration.LanguageLevel.JAVA_13),
                Arguments.of("JAVA_1_14", ParserConfiguration.LanguageLevel.JAVA_14),
                Arguments.of("JAVA_1_14_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_14_PREVIEW),
                Arguments.of("JAVA_1_15", ParserConfiguration.LanguageLevel.JAVA_15),
                Arguments.of("JAVA_1_15_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_15_PREVIEW),
                Arguments.of("JAVA_1_16", ParserConfiguration.LanguageLevel.JAVA_16),
                Arguments.of("JAVA_1_16_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_16_PREVIEW),
                Arguments.of("JAVA_1_17", ParserConfiguration.LanguageLevel.JAVA_17),
                Arguments.of("JAVA_1_17_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_17_PREVIEW)
        );
    }


    private static Stream<Arguments> mappingJdk_1() {
        return Stream.of(
                Arguments.of("JDK_1_X", ParserConfiguration.LanguageLevel.BLEEDING_EDGE),
                Arguments.of("JDK_1_0", ParserConfiguration.LanguageLevel.JAVA_1_0),
                Arguments.of("JDK_1_1", ParserConfiguration.LanguageLevel.JAVA_1_1),
                Arguments.of("JDK_1_2", ParserConfiguration.LanguageLevel.JAVA_1_2),
                Arguments.of("JDK_1_3", ParserConfiguration.LanguageLevel.JAVA_1_3),
                Arguments.of("JDK_1_4", ParserConfiguration.LanguageLevel.JAVA_1_4),
                Arguments.of("JDK_1_5", ParserConfiguration.LanguageLevel.JAVA_5),
                Arguments.of("JDK_1_6", ParserConfiguration.LanguageLevel.JAVA_6),
                Arguments.of("JDK_1_7", ParserConfiguration.LanguageLevel.JAVA_7),
                Arguments.of("JDK_1_8", ParserConfiguration.LanguageLevel.JAVA_8),
                Arguments.of("JDK_1_9", ParserConfiguration.LanguageLevel.JAVA_9),
                Arguments.of("JDK_1_10", ParserConfiguration.LanguageLevel.JAVA_10),
                Arguments.of("JDK_1_11", ParserConfiguration.LanguageLevel.JAVA_11),
                Arguments.of("JDK_1_12", ParserConfiguration.LanguageLevel.JAVA_12),
                Arguments.of("JDK_1_13", ParserConfiguration.LanguageLevel.JAVA_13),
                Arguments.of("JDK_1_14", ParserConfiguration.LanguageLevel.JAVA_14),
                Arguments.of("JDK_1_14_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_14_PREVIEW),
                Arguments.of("JDK_1_15", ParserConfiguration.LanguageLevel.JAVA_15),
                Arguments.of("JDK_1_15_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_15_PREVIEW),
                Arguments.of("JDK_1_16", ParserConfiguration.LanguageLevel.JAVA_16),
                Arguments.of("JDK_1_16_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_16_PREVIEW),
                Arguments.of("JDK_1_17", ParserConfiguration.LanguageLevel.JAVA_17),
                Arguments.of("JDK_1_17_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_17_PREVIEW)
        );
    }


    private static Stream<Arguments> mappingJava() {
        return Stream.of(
                Arguments.of("JAVA_X", ParserConfiguration.LanguageLevel.BLEEDING_EDGE),
                Arguments.of("JAVA_0", ParserConfiguration.LanguageLevel.JAVA_1_0),
                Arguments.of("JAVA_1", ParserConfiguration.LanguageLevel.JAVA_1_1),
                Arguments.of("JAVA_2", ParserConfiguration.LanguageLevel.JAVA_1_2),
                Arguments.of("JAVA_3", ParserConfiguration.LanguageLevel.JAVA_1_3),
                Arguments.of("JAVA_4", ParserConfiguration.LanguageLevel.JAVA_1_4),
                Arguments.of("JAVA_5", ParserConfiguration.LanguageLevel.JAVA_5),
                Arguments.of("JAVA_6", ParserConfiguration.LanguageLevel.JAVA_6),
                Arguments.of("JAVA_7", ParserConfiguration.LanguageLevel.JAVA_7),
                Arguments.of("JAVA_8", ParserConfiguration.LanguageLevel.JAVA_8),
                Arguments.of("JAVA_9", ParserConfiguration.LanguageLevel.JAVA_9),
                Arguments.of("JAVA_10", ParserConfiguration.LanguageLevel.JAVA_10),
                Arguments.of("JAVA_11", ParserConfiguration.LanguageLevel.JAVA_11),
                Arguments.of("JAVA_12", ParserConfiguration.LanguageLevel.JAVA_12),
                Arguments.of("JAVA_13", ParserConfiguration.LanguageLevel.JAVA_13),
                Arguments.of("JAVA_14", ParserConfiguration.LanguageLevel.JAVA_14),
                Arguments.of("JAVA_14_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_14_PREVIEW),
                Arguments.of("JAVA_15", ParserConfiguration.LanguageLevel.JAVA_15),
                Arguments.of("JAVA_15_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_15_PREVIEW),
                Arguments.of("JAVA_16", ParserConfiguration.LanguageLevel.JAVA_16),
                Arguments.of("JAVA_16_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_16_PREVIEW),
                Arguments.of("JAVA_17", ParserConfiguration.LanguageLevel.JAVA_17),
                Arguments.of("JAVA_17_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_17_PREVIEW)
        );
    }


    private static Stream<Arguments> mappingJdk() {
        return Stream.of(
                Arguments.of("JDK_X", ParserConfiguration.LanguageLevel.BLEEDING_EDGE),
                Arguments.of("JDK_0", ParserConfiguration.LanguageLevel.JAVA_1_0),
                Arguments.of("JDK_1", ParserConfiguration.LanguageLevel.JAVA_1_1),
                Arguments.of("JDK_2", ParserConfiguration.LanguageLevel.JAVA_1_2),
                Arguments.of("JDK_3", ParserConfiguration.LanguageLevel.JAVA_1_3),
                Arguments.of("JDK_4", ParserConfiguration.LanguageLevel.JAVA_1_4),
                Arguments.of("JDK_5", ParserConfiguration.LanguageLevel.JAVA_5),
                Arguments.of("JDK_6", ParserConfiguration.LanguageLevel.JAVA_6),
                Arguments.of("JDK_7", ParserConfiguration.LanguageLevel.JAVA_7),
                Arguments.of("JDK_8", ParserConfiguration.LanguageLevel.JAVA_8),
                Arguments.of("JDK_9", ParserConfiguration.LanguageLevel.JAVA_9),
                Arguments.of("JDK_10", ParserConfiguration.LanguageLevel.JAVA_10),
                Arguments.of("JDK_11", ParserConfiguration.LanguageLevel.JAVA_11),
                Arguments.of("JDK_12", ParserConfiguration.LanguageLevel.JAVA_12),
                Arguments.of("JDK_13", ParserConfiguration.LanguageLevel.JAVA_13),
                Arguments.of("JDK_14", ParserConfiguration.LanguageLevel.JAVA_14),
                Arguments.of("JDK_14_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_14_PREVIEW),
                Arguments.of("JDK_15", ParserConfiguration.LanguageLevel.JAVA_15),
                Arguments.of("JDK_15_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_15_PREVIEW),
                Arguments.of("JDK_16", ParserConfiguration.LanguageLevel.JAVA_16),
                Arguments.of("JDK_16_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_16_PREVIEW),
                Arguments.of("JDK_17", ParserConfiguration.LanguageLevel.JAVA_17),
                Arguments.of("JDK_17_PREVIEW", ParserConfiguration.LanguageLevel.JAVA_17_PREVIEW)
        );
    }


    private static Stream<Arguments> mappingInvalidDefault() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("1234"),
                Arguments.of("ABC"),
                Arguments.of("JAVA_"),
                Arguments.of("JDK_")
        );
    }


    @ParameterizedTest
    @MethodSource("mappingNoPrefix")
    void stringMappingNoPrefixIsFound(String projectLanguageLevel, ParserConfiguration.LanguageLevel exectedJavaParserLanguageLevel) {
        assertEquals(
                exectedJavaParserLanguageLevel,
                LanguageLevelUtil.mapStringLanguageLevelToJavaParserLanguageLevel(projectLanguageLevel).orElseThrow()
        );
    }


    @ParameterizedTest
    @MethodSource("mappingJava_1")
    void stringMappingJava_1IsFound(String projectLanguageLevel, ParserConfiguration.LanguageLevel exectedJavaParserLanguageLevel) {
        assertEquals(
                exectedJavaParserLanguageLevel,
                LanguageLevelUtil.mapStringLanguageLevelToJavaParserLanguageLevel(projectLanguageLevel).orElseThrow()
        );
    }


    @ParameterizedTest
    @MethodSource("mappingJdk_1")
    void stringMappingJdk_1IsFound(String projectLanguageLevel, ParserConfiguration.LanguageLevel exectedJavaParserLanguageLevel) {
        assertEquals(
                exectedJavaParserLanguageLevel,
                LanguageLevelUtil.mapStringLanguageLevelToJavaParserLanguageLevel(projectLanguageLevel).orElseThrow()
        );
    }


    @ParameterizedTest
    @MethodSource("mappingJava")
    void stringMappingJavaIsFound(String projectLanguageLevel, ParserConfiguration.LanguageLevel exectedJavaParserLanguageLevel) {
        assertEquals(
                exectedJavaParserLanguageLevel,
                LanguageLevelUtil.mapStringLanguageLevelToJavaParserLanguageLevel(projectLanguageLevel).orElseThrow()
        );
    }


    @ParameterizedTest
    @MethodSource("mappingJdk")
    void stringMappingJdkIsFound(String projectLanguageLevel, ParserConfiguration.LanguageLevel exectedJavaParserLanguageLevel) {
        assertEquals(
                exectedJavaParserLanguageLevel,
                LanguageLevelUtil.mapStringLanguageLevelToJavaParserLanguageLevel(projectLanguageLevel).orElseThrow()
        );
    }


    @ParameterizedTest
    @MethodSource("mappingInvalidDefault")
    void stringMappingIsNotFound(String projectLanguageLevel) {
        assertFalse(
                LanguageLevelUtil.mapStringLanguageLevelToJavaParserLanguageLevel(projectLanguageLevel).isPresent()
        );
    }


    @ParameterizedTest
    @EnumSource(LanguageLevel.class)
    void allIntellijLanguageLevelsAreValid(LanguageLevel intellijLanguageLevel) {

        // These language levels are known to be unsupported.
        Set<LanguageLevel> jpUnsupported = Set.of(
                LanguageLevel.JDK_17,
                LanguageLevel.JDK_17_PREVIEW,
                LanguageLevel.JDK_18,
                LanguageLevel.JDK_18_PREVIEW
        );
        assumeFalse(
                jpUnsupported.contains(intellijLanguageLevel),
                "Given IntelliJ language level `" + intellijLanguageLevel + "` known to be unsupported by JavaParser."
        );

        assertTrue(
                LanguageLevelUtil.mapIntellijLanguageLevelToJavaParserLanguageLevel(intellijLanguageLevel).isPresent(),
                "Given IntelliJ language level `" + intellijLanguageLevel + "` not supported by JavaParser, and has not been explicitly added to the known unsupported list."
        );
    }
}