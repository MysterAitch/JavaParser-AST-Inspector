package com.github.rogerhowell.javaparser_ast_inspector.plugin.util;

import com.github.javaparser.ParserConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.pom.java.LanguageLevel;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Optional;

public final class LanguageLevelUtil {

    private LanguageLevelUtil() {
        // Prevent initialisation
    }


    @NotNull
    public static ParserConfiguration.LanguageLevel getLanguageLevelFromProject(@NotNull Project project) {
        final LanguageLevelProjectExtension languageLevelProjectExtension = LanguageLevelProjectExtension.getInstance(project);
        final LanguageLevel                 projectLanguageLevel          = languageLevelProjectExtension.getLanguageLevel();

        Optional<ParserConfiguration.LanguageLevel> selectedLanguageLevel = mapIntellijLanguageLevelToJavaParserLanguageLevel(projectLanguageLevel);
        return selectedLanguageLevel.orElseGet(() -> {
            System.out.printf("Mapping for IntelliJ Language Level (`%s`) not found, defaulting to JavaParser's `ParserConfiguration.LanguageLevel.CURRENT`.", projectLanguageLevel.name());
            // Default to whatever the "CURRENT" version is if it isn't found.
            return ParserConfiguration.LanguageLevel.CURRENT;
        });
    }


    @NotNull
    public static Optional<ParserConfiguration.LanguageLevel> mapIntellijLanguageLevelToJavaParserLanguageLevel(@NotNull LanguageLevel projectLanguageLevel) {
        // Note that not all of these will be present in each IDE
        // Hence using String as the key, instead of LanguageLevel
        final String languageLevelName = projectLanguageLevel.name();

        return mapStringLanguageLevelToJavaParserLanguageLevel(languageLevelName);
    }


    @NotNull
    public static Optional<ParserConfiguration.LanguageLevel> mapStringLanguageLevelToJavaParserLanguageLevel(final String languageLevelName) {
        final HashMap<String, ParserConfiguration.LanguageLevel> languageLevelMap = new HashMap<>();

        /*
         * Iterate over all known JavaParser language levels.
         * Note that this will include preview versions too.
         */
        for (ParserConfiguration.LanguageLevel javaParserLanguageLevel : ParserConfiguration.LanguageLevel.values()) {

            String plainLanguageLevelName = javaParserLanguageLevel
                    .name()
                    .replace("JAVA_1_", "JAVA_")
                    .replace("JAVA_", "");


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

        ParserConfiguration.LanguageLevel selectedLanguageLevel = languageLevelMap.get(languageLevelName);
        return Optional.ofNullable(selectedLanguageLevel);
    }
}
