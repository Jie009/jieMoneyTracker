plugins {
    `kotlin-dsl`
}

group = "com.budgettracker.buildlogic"

dependencies {
    implementation("com.android.tools.build:gradle:8.7.3")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.0.21")
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.0.21-1.0.28")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.52")
    implementation("androidx.room:room-gradle-plugin:2.6.1")
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "budgettracker.android.application"
            implementationClass = "BudgetTrackerAndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "budgettracker.android.library"
            implementationClass = "BudgetTrackerAndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "budgettracker.android.feature"
            implementationClass = "BudgetTrackerAndroidFeatureConventionPlugin"
        }
        register("androidCompose") {
            id = "budgettracker.android.compose"
            implementationClass = "BudgetTrackerAndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "budgettracker.android.hilt"
            implementationClass = "BudgetTrackerAndroidHiltConventionPlugin"
        }
        register("androidRoom") {
            id = "budgettracker.android.room"
            implementationClass = "BudgetTrackerAndroidRoomConventionPlugin"
        }
        register("kotlinLibrary") {
            id = "budgettracker.kotlin.library"
            implementationClass = "BudgetTrackerKotlinLibraryConventionPlugin"
        }
    }
}
