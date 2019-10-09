import kotlin.String
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

/**
 * Generated by https://github.com/jmfayard/buildSrcVersions
 *
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version.
 */
object Versions {
    const val org_danilopianini_git_sensitive_semantic_versioning_gradle_plugin: String = "0.2.2"

    const val org_danilopianini_publish_on_central_gradle_plugin: String = "0.2.2"

    const val org_jlleitschuh_gradle_ktlint_gradle_plugin: String = "9.0.0"

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.6.4"

    const val org_jetbrains_kotlin_jvm_gradle_plugin: String = "1.3.50"

    const val org_protelis_protelisdoc_gradle_plugin: String = "0.2.0"

    const val com_gradle_build_scan_gradle_plugin: String = "2.4.2"

    const val com_github_spotbugs_gradle_plugin: String = "1.6.9" // available: "2.0.0"

    const val com_jfrog_bintray_gradle_plugin: String = "1.8.4"

    const val org_jetbrains_kotlin: String = "1.3.50"

    const val protelis_interpreter: String = "13.0.3"

    const val spotbugs_annotations: String = "3.1.12"

    const val it_unibo_alchemist: String = "4.0.0" // available: "9.1.0"

    const val logback_classic: String = "1.3.0-alpha4"

    const val protelis_parser: String = "10.0.1"

    const val commons_codec: String = "1.13"

    const val commons_lang3: String = "3.9"

    const val commons_math3: String = "3.6.1"

    const val classgraph: String = "4.8.47"

    const val commons_io: String = "2.6"

    const val slf4j_api: String = "1.7.28"

    const val trove4j: String = "3.0.3"

    const val apiviz: String = "1.3.2.GA"

    const val ktlint: String = "0.34.2"

    const val guava: String = "28.0-jre"

    const val junit: String = "4.13-beta-3"

    const val fst: String = "2.57"

    /**
     * Current version: "5.6.2"
     * See issue 19: How to update Gradle itself?
     * https://github.com/jmfayard/buildSrcVersions/issues/19
     */
    const val gradleLatestVersion: String = "5.6.2"
}

/**
 * See issue #47: how to update buildSrcVersions itself
 * https://github.com/jmfayard/buildSrcVersions/issues/47
 */
val PluginDependenciesSpec.buildSrcVersions: PluginDependencySpec
    inline get() =
            id("de.fayard.buildSrcVersions").version(Versions.de_fayard_buildsrcversions_gradle_plugin)
