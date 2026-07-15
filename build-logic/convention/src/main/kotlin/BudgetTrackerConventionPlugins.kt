import androidx.room.gradle.RoomExtension
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class BudgetTrackerAndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.android")

        extensions.configure<ApplicationExtension> {
            namespace = "com.budgettracker.app"
            compileSdk = 35

            defaultConfig {
                applicationId = "com.budgettracker"
                minSdk = 26
                targetSdk = 35
                versionCode = 1
                versionName = "0.1.0"
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            configureAndroidCommon()
        }

        configureKotlinAndroid()
    }
}

class BudgetTrackerAndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")

        extensions.configure<LibraryExtension> {
            namespace = moduleNamespace()
            compileSdk = 35

            defaultConfig {
                minSdk = 26
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            configureAndroidCommon()
        }

        configureKotlinAndroid()
    }
}

class BudgetTrackerAndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("budgettracker.android.library")

        dependencies {
            "implementation"(project(":core:common"))
        }
    }
}

class BudgetTrackerAndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        pluginManager.withPlugin("com.android.application") {
            extensions.configure<ApplicationExtension> {
                buildFeatures.compose = true
            }
        }
        pluginManager.withPlugin("com.android.library") {
            extensions.configure<LibraryExtension> {
                buildFeatures.compose = true
            }
        }

        dependencies {
            "implementation"(platform(libs.findLibrary("androidx-compose-bom").get()))
            "androidTestImplementation"(platform(libs.findLibrary("androidx-compose-bom").get()))
            "implementation"(libs.findLibrary("androidx-compose-ui").get())
            "implementation"(libs.findLibrary("androidx-compose-foundation").get())
            "implementation"(libs.findLibrary("androidx-compose-material-icons-extended").get())
            "implementation"(libs.findLibrary("androidx-compose-material3").get())
            "implementation"(libs.findLibrary("androidx-compose-runtime").get())
            "implementation"(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
        }
    }
}

class BudgetTrackerAndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.dagger.hilt.android")
        pluginManager.apply("com.google.devtools.ksp")

        dependencies {
            "implementation"(libs.findLibrary("hilt-android").get())
            "ksp"(libs.findLibrary("hilt-compiler").get())
        }
    }
}

class BudgetTrackerAndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("androidx.room")
        pluginManager.apply("com.google.devtools.ksp")

        extensions.configure<RoomExtension> {
            schemaDirectory("$projectDir/schemas")
        }

        dependencies {
            "implementation"(libs.findLibrary("androidx-room-runtime").get())
            "implementation"(libs.findLibrary("androidx-room-ktx").get())
            "ksp"(libs.findLibrary("androidx-room-compiler").get())
        }
    }
}

class BudgetTrackerKotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(17)
        }
    }
}

private val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

private fun ApplicationExtension.configureAndroidCommon() {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

private fun LibraryExtension.configureAndroidCommon() {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

private fun Project.configureKotlinAndroid() {
    extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(17)
    }
}

private fun Project.moduleNamespace(): String {
    val modulePath = path.removePrefix(":")
    val namespaceSuffix = modulePath
        .split(":")
        .joinToString(".") { segment -> segment.replace("-", "") }

    return "com.budgettracker.$namespaceSuffix"
}
