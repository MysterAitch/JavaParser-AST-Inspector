import com.jfrog.bintray.gradle.BintrayExtension
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.closure
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "0.6.2"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "0.6.2"
    // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
    id("io.gitlab.arturbosch.detekt") version "1.14.2"

    id("com.jfrog.bintray") version "1.8.5"
}

// Import variables from gradle.properties file
val pluginGroup: String by project
// `pluginName_` variable ends with `_` because of the collision with Kotlin magic getter in the `intellij` closure.
// Read more about the issue: https://github.com/JetBrains/intellij-platform-plugin-template/issues/29
val pluginName_: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project
val pluginVerifierIdeVersions: String by project

val platformType: String by project
val platformVersion: String by project
val platformPlugins: String by project
val platformDownloadSources: String by project

group = pluginGroup
version = pluginVersion

val javaVersion = "1.8"
//val javaVersion = JavaVersion.VERSION_1_8
//val sourceCompatibilityVersion = javaVersion
//val targetCompatibilityVersion = javaVersion

val artifactName = project.name
val artifactGroup = project.group.toString()
val artifactVersion = project.version.toString()

// Configure project's dependencies
repositories {
    mavenCentral()
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.14.2")
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.16.2")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("guru.nidi:graphviz-java-all-j2v8:0.17.1")
    implementation("org.apache.logging.log4j:log4j-core:2.13.3")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.3")
    testImplementation("junit:junit:4.13.1")
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName = pluginName_
    version = platformVersion
    type = platformType
    downloadSources = platformDownloadSources.toBoolean()
    updateSinceUntilBuild = true

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    setPlugins(*platformPlugins.split(',').map(String::trim).filter(String::isNotEmpty).toTypedArray())
}

// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
    config = files("./detekt-config.yml")
    buildUponDefaultConfig = true

    reports {
        html.enabled = false
        xml.enabled = false
        txt.enabled = false
    }
}

tasks {
    // Set the compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    listOf("compileKotlin", "compileTestKotlin").forEach {
        getByName<KotlinCompile>(it) {
            kotlinOptions.jvmTarget = javaVersion
        }
    }

    withType<Detekt> {
        jvmTarget = javaVersion
    }

    patchPluginXml {
        version(pluginVersion)
        sinceBuild(pluginSinceBuild)
        untilBuild(pluginUntilBuild)

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription(
                closure {
                    File("./README.md").readText().lines().run {
                        val start = "<!-- Plugin description -->"
                        val end = "<!-- Plugin description end -->"

                        if (!containsAll(listOf(start, end))) {
                            throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                        }
                        subList(indexOf(start) + 1, indexOf(end))
                    }.joinToString("\n").run { markdownToHTML(this) }
                }
        )

        // Get the latest available change notes from the changelog file
        changeNotes(
                closure {
                    changelog.getLatest().toHTML()
                }
        )
    }

    runPluginVerifier {
        ideVersions(pluginVerifierIdeVersions)
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token(System.getenv("INTELLIJ_PLUGIN_PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://jetbrains.org/intellij/sdk/docs/tutorials/build_system/deployment.html#specifying-a-release-channel
        channels(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first())
    }
}


// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
    config = files("./detekt-config.yml")
    buildUponDefaultConfig = true

    reports {
        html.enabled = false
        xml.enabled = false
        txt.enabled = false
    }
}


//changelog {
//    version = "${project.version}"
//    path = "${project.projectDir}/CHANGELOG.md"
//    header = closure { "[${project.version}] - ${date()}" }
//    headerParserRegex = """\d+\.\d+\.\d+""".toRegex()
//    itemPrefix = "-"
//    keepUnreleasedSection = true
//    unreleasedTerm = "[Unreleased]"
//    groups = listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security")
//}


// https://github.com/bintray/gradle-bintray-plugin#readme
bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_API_KEY")

//    configurations = ["archives"] // Upload the archives to bintray

    dryRun = true //[Default: false] Whether to run this as dry-run, without deploying
    publish = false //[Default: false] Whether version should be auto published after an upload
    override = false //[Default: false] Whether to override version artifacts already published

    //Package configuration. The plugin will use the repo and name properties to check if the package already exists. In that case, there"s no need to configure the other package properties (like userOrg, desc, etc).
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "JavaParser-AST-Inspector"
        name = "${project.group}:${project.name}"

        setLicenses("MIT")

        websiteUrl = "https://github.com/MysterAitch/JavaParser-AST-Inspector"
        issueTrackerUrl = "https://github.com/MysterAitch/JavaParser-AST-Inspector/issues"
        vcsUrl = "https://github.com/MysterAitch/JavaParser-AST-Inspector.git"

        githubRepo = "MysterAitch/JavaParser-AST-Inspector" //Optional Github repository
        githubReleaseNotesFile = "CHANGELOG.md" //Optional Github readme file

        with(version) {
            name = artifactVersion
        }
    })
}
