import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    // Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "1.15.0"
    // Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "2.1.2"
    // Gradle Qodana Plugin
    id("org.jetbrains.qodana") version "0.1.13"
    // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
    id("io.gitlab.arturbosch.detekt") version "1.20.0"
    // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"

    id("jacoco")
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

val javaParserVersion = "3.25.5"

// Configure project's dependencies
repositories {
    mavenCentral()
}
dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.20.0")

    implementation("com.github.javaparser:javaparser-symbol-solver-core:$javaParserVersion"){
        exclude(group = "com.google.guava")
        exclude(group = "org.javassist")
    }
    implementation("org.apache.commons:commons-text:1.10.0"){
        exclude(group = "*")
    }
    // implementation("guru.nidi:graphviz-java-all-j2v8:0.18.1")
    // implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    // implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

// Set the JVM language level used to build the project. Use Java 11 for 2020.3+, and Java 17 for 2022.2+.
kotlin {
    jvmToolchain(17)
}

// Configure plugin `Gradle IntelliJ Plugin`.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    downloadSources = true
//    downloadSources = properties("platformDownloadSources")
    updateSinceUntilBuild = true

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}
// Configure plugin `Gradle Changelog Plugin`.
// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = properties("pluginRepositoryUrl")
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
qodana {
    cachePath = provider { file(".qodana").canonicalPath }
    reportPath = provider { file("build/reports/inspections").canonicalPath }
    saveReport = true
    showReport = environment("QODANA_SHOW_REPORT").map { it.toBoolean() }.getOrElse(false)
}

// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
    config = files("./detekt-config.yml")
    buildUponDefaultConfig = true
}

tasks.withType<Detekt>().configureEach {
    reports {
        xml.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
    }
}

tasks {
    initializeIntelliJPlugin{
        offline = true
    }

    buildSearchableOptions{
        enabled = false
    }

    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
                    .joinToString("\n")
                    .replace(Regex("v\\d+\\.\\d+\\.\\d+"), "3.25.5")
                    .let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML
                )
            }
        }
    }

    runPluginVerifier {
        ideVersions = properties("pluginVerifierIdeVersions").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    signPlugin {
        certificateChain = environment("CERTIFICATE_CHAIN")
        privateKey = environment("PRIVATE_KEY")
        password = environment("PRIVATE_KEY_PASSWORD")
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = properties("pluginVersion").map { listOf(it.split('-').getOrElse(1) { "default" }.split('.').first()) }
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(true)
    }
}

// https://docs.gradle.org/current/dsl/org.gradle.api.tasks.testing.Test.html
// https://docs.gradle.org/current/userguide/java_testing.html#using_junit5
tasks.test {
    // enable JUnit Platform (a.k.a. JUnit 5) support
    useJUnitPlatform()

    finalizedBy("jacocoTestReport")
    doLast {
        logger.info("View code coverage at:")
        logger.info("file://${layout.buildDirectory}/reports/jacoco/test/html/index.html")
    }
}
