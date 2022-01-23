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
        for (ParserConfiguration.LanguageLevel javaParserLanguageLevel : ParserConfiguration.LanguageLevel.values()) {
            // JavaParser uses `JAVA_{}`, while IntelliJ uses `JDK_{}`.
            String javaParserLanguageLevelName = javaParserLanguageLevel.name();
            languageLevelMap.put(javaParserLanguageLevelName, javaParserLanguageLevel);

            // Some versions have a `1.` prefix (e.g. JDK 6 is also known as 1.6, thus is listed as JDK_1_6 as opposed to JDK_6).
            String javaParserLanguageLevelNameWith1_Prefix = javaParserLanguageLevel.name().replace("JAVA_", "JAVA_1_");
            languageLevelMap.put(javaParserLanguageLevelNameWith1_Prefix, javaParserLanguageLevel);

            // JavaParser uses `JAVA_{}`, while IntelliJ uses `JDK_{}`.
            String intellijLanguageLevelName = javaParserLanguageLevel.name().replace("JAVA_", "JDK_");
            languageLevelMap.put(intellijLanguageLevelName, javaParserLanguageLevel);

            // Some versions have a `1.` prefix (e.g. JDK 6 is also known as 1.6, thus is listed as JDK_1_6 as opposed to JDK_6).
            String intellijLanguageLevelNameWith1_Prefix = javaParserLanguageLevel.name().replace("JAVA_", "JDK_1_");
            languageLevelMap.put(intellijLanguageLevelNameWith1_Prefix, javaParserLanguageLevel);
        }

        ParserConfiguration.LanguageLevel selectedLanguageLevel = languageLevelMap.get(languageLevelName);
        return Optional.ofNullable(selectedLanguageLevel);
    }
}
