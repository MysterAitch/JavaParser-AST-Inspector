package com.github.rogerhowell.javaparser_ast_inspector.plugin.util;

import com.github.javaparser.ParserConfiguration;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.logging.NotificationLogger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.pom.java.LanguageLevel;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Optional;

public final class LanguageLevelUtil {

    private static final NotificationLogger notificationLogger = new NotificationLogger(LanguageLevelUtil.class);

    private static final HashMap<String, ParserConfiguration.LanguageLevel> languageLevelMap = new HashMap<>();

    static {
        initMappings();
    }


    private LanguageLevelUtil() {
        // Prevent initialisation
    }


    public static ParserConfiguration.LanguageLevel getLanguageLevelFromProject(@NotNull Project project, @NotNull ParserConfiguration.LanguageLevel defaultJpLanguageLevel) {
        final LanguageLevelProjectExtension languageLevelProjectExtension = LanguageLevelProjectExtension.getInstance(project);
        final LanguageLevel                 projectLanguageLevel          = languageLevelProjectExtension.getLanguageLevel();

        return mapIntellijLanguageLevelToJavaParserLanguageLevel(projectLanguageLevel, defaultJpLanguageLevel);
    }


    public static Optional<ParserConfiguration.LanguageLevel> getLanguageLevelFromProject(@NotNull Project project) {
        final LanguageLevelProjectExtension languageLevelProjectExtension = LanguageLevelProjectExtension.getInstance(project);
        final LanguageLevel                 projectLanguageLevel          = languageLevelProjectExtension.getLanguageLevel();

        return mapIntellijLanguageLevelToJavaParserLanguageLevel(projectLanguageLevel);
    }


    private static void initMappings() {
        /*
         * Iterate over all known JavaParser language levels.
         * Note that this will include preview versions too.
         */
        for (ParserConfiguration.LanguageLevel javaParserLanguageLevel : ParserConfiguration.LanguageLevel.values()) {

            String plainLanguageLevelName = javaParserLanguageLevel.name().replace("JAVA_1_", "JAVA_").replace("JAVA_", "");


            // No prefix
            languageLevelMap.put(plainLanguageLevelName, javaParserLanguageLevel);
            // JavaParser uses `JAVA_{}`
            languageLevelMap.put("JAVA_" + plainLanguageLevelName, javaParserLanguageLevel);
            // IntelliJ uses `JDK_{}`.
            languageLevelMap.put("JDK_" + plainLanguageLevelName, javaParserLanguageLevel);

            // Some versions have a `1.` prefix (e.g. JDK 6 is also known as 1.6, thus is listed as JDK_1_6 as opposed to JDK_6).
            // This is a bit of a hacky catch-all, given that not all places are consistent (e.g. JP has JAVA_6, but IntelliJ has JDK_1_6
            languageLevelMap.put("JAVA_1_" + plainLanguageLevelName, javaParserLanguageLevel);
            languageLevelMap.put("JDK_1_" + plainLanguageLevelName, javaParserLanguageLevel);
        }

        // IntelliJ also contains "JDK X" which includes "experimental features" - i.e., latest preview
        languageLevelMap.put("X", ParserConfiguration.LanguageLevel.BLEEDING_EDGE);
        languageLevelMap.put("JAVA_X", ParserConfiguration.LanguageLevel.BLEEDING_EDGE);
        languageLevelMap.put("JDK_X", ParserConfiguration.LanguageLevel.BLEEDING_EDGE);
        languageLevelMap.put("JAVA_1_X", ParserConfiguration.LanguageLevel.BLEEDING_EDGE);
        languageLevelMap.put("JDK_1_X", ParserConfiguration.LanguageLevel.BLEEDING_EDGE);
    }


    @NotNull
    public static ParserConfiguration.LanguageLevel mapIntellijLanguageLevelToJavaParserLanguageLevel(@NotNull LanguageLevel intellijLanguageLevel, @NotNull ParserConfiguration.LanguageLevel defaultJpLanguageLevel) {
        final String intellijLanguageLevelName = intellijLanguageLevel.name();
        return mapStringLanguageLevelToJavaParserLanguageLevel(intellijLanguageLevelName, defaultJpLanguageLevel);
    }


    @NotNull
    public static Optional<ParserConfiguration.LanguageLevel> mapIntellijLanguageLevelToJavaParserLanguageLevel(@NotNull LanguageLevel intellijLanguageLevel) {
        final String intellijLanguageLevelName = intellijLanguageLevel.name();
        return mapStringLanguageLevelToJavaParserLanguageLevel(intellijLanguageLevelName);
    }


    @NotNull
    public static ParserConfiguration.LanguageLevel mapStringLanguageLevelToJavaParserLanguageLevel(@NotNull final String languageLevelName, @NotNull ParserConfiguration.LanguageLevel defaultJpLanguageLevel) {
        return mapStringLanguageLevelToJavaParserLanguageLevel(languageLevelName).orElseGet(() -> {
            notificationLogger.warn("Mapping for IntelliJ Language Level (`" + languageLevelName + "`) not found, defaulting to JavaParser's `" + defaultJpLanguageLevel + "`.");
            return defaultJpLanguageLevel;
        });
    }


    @NotNull
    public static Optional<ParserConfiguration.LanguageLevel> mapStringLanguageLevelToJavaParserLanguageLevel(@NotNull final String languageLevelName) {
        ParserConfiguration.LanguageLevel mappedLanguageLevel = languageLevelMap.get(languageLevelName);
        return Optional.ofNullable(mappedLanguageLevel);
    }

}
