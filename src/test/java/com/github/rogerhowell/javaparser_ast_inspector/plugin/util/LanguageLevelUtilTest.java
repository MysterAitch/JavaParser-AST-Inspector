package com.github.rogerhowell.javaparser_ast_inspector.plugin.util;

import com.github.javaparser.ParserConfiguration;
import com.intellij.pom.java.LanguageLevel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LanguageLevelUtilTest {


    private static Stream<Arguments> mappingJava_1() {
        return Stream.of(
                Arguments.of("JAVA_1_0", ParserConfiguration.LanguageLevel.JAVA_1_0),
                Arguments.of("JAVA_1_1", ParserConfiguration.LanguageLevel.JAVA_1_1),
                Arguments.of("JAVA_1_2", ParserConfiguration.LanguageLevel.JAVA_1_2),
                Arguments.of("JAVA_1_3", ParserConfiguration.LanguageLevel.JAVA_1_3),
                Arguments.of("JAVA_1_4", ParserConfiguration.LanguageLevel.JAVA_1_4),
                Arguments.of("JAVA_1_5", ParserConfiguration.LanguageLevel.JAVA_5),
                Arguments.of("JAVA_1_6", ParserConfiguration.LanguageLevel.JAVA_6),
                Arguments.of("JAVA_1_7", ParserConfiguration.LanguageLevel.JAVA_7),
                Arguments.of("JAVA_1_8", ParserConfiguration.LanguageLevel.JAVA_8),
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

    private static Stream<Arguments> mappingJava() {
        return Stream.of(
                Arguments.of("JAVA_1_0", ParserConfiguration.LanguageLevel.JAVA_1_0),
                Arguments.of("JAVA_1_1", ParserConfiguration.LanguageLevel.JAVA_1_1),
                Arguments.of("JAVA_1_2", ParserConfiguration.LanguageLevel.JAVA_1_2),
                Arguments.of("JAVA_1_3", ParserConfiguration.LanguageLevel.JAVA_1_3),
                Arguments.of("JAVA_1_4", ParserConfiguration.LanguageLevel.JAVA_1_4),
                Arguments.of("JAVA_1_5", ParserConfiguration.LanguageLevel.JAVA_5),
                Arguments.of("JAVA_1_6", ParserConfiguration.LanguageLevel.JAVA_6),
                Arguments.of("JAVA_1_7", ParserConfiguration.LanguageLevel.JAVA_7),
                Arguments.of("JAVA_1_8", ParserConfiguration.LanguageLevel.JAVA_8),
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
                Arguments.of("JDK_1_0", ParserConfiguration.LanguageLevel.JAVA_1_0),
                Arguments.of("JDK_1_1", ParserConfiguration.LanguageLevel.JAVA_1_1),
                Arguments.of("JDK_1_2", ParserConfiguration.LanguageLevel.JAVA_1_2),
                Arguments.of("JDK_1_3", ParserConfiguration.LanguageLevel.JAVA_1_3),
                Arguments.of("JDK_1_4", ParserConfiguration.LanguageLevel.JAVA_1_4),
                Arguments.of("JDK_1_5", ParserConfiguration.LanguageLevel.JAVA_5),
                Arguments.of("JDK_1_6", ParserConfiguration.LanguageLevel.JAVA_6),
                Arguments.of("JDK_1_7", ParserConfiguration.LanguageLevel.JAVA_7),
                Arguments.of("JDK_1_8", ParserConfiguration.LanguageLevel.JAVA_8),
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
                Arguments.of("0"),
                Arguments.of("ABC"),
                Arguments.of("JAVA_"),
                Arguments.of("JDK_")
        );
    }



    @ParameterizedTest
    @MethodSource("mappingJava_1")
    void stringMappingJava_1Found(String projectLanguageLevel, ParserConfiguration.LanguageLevel exectedJavaParserLanguageLevel) {
        assertEquals(
                exectedJavaParserLanguageLevel,
                LanguageLevelUtil.mapStringLanguageLevelToJavaParserLanguageLevel(projectLanguageLevel).orElseThrow()
        );
    }

    @ParameterizedTest
    @MethodSource("mappingJava")
    void stringMappingJavaFound(String projectLanguageLevel, ParserConfiguration.LanguageLevel exectedJavaParserLanguageLevel) {
        assertEquals(
                exectedJavaParserLanguageLevel,
                LanguageLevelUtil.mapStringLanguageLevelToJavaParserLanguageLevel(projectLanguageLevel).orElseThrow()
        );
    }

    @ParameterizedTest
    @MethodSource("mappingJdk")
    void stringMappingJdkFound(String projectLanguageLevel, ParserConfiguration.LanguageLevel exectedJavaParserLanguageLevel) {
        assertEquals(
                exectedJavaParserLanguageLevel,
                LanguageLevelUtil.mapStringLanguageLevelToJavaParserLanguageLevel(projectLanguageLevel).orElseThrow()
        );
    }

    @ParameterizedTest
    @MethodSource("mappingInvalidDefault")
    void stringMappingNotFound(String projectLanguageLevel) {
        assertFalse(
                LanguageLevelUtil.mapStringLanguageLevelToJavaParserLanguageLevel(projectLanguageLevel).isPresent()
        );
    }
}