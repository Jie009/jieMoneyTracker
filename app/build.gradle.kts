plugins {
    id("budgettracker.android.application")
    id("budgettracker.android.compose")
    id("budgettracker.android.hilt")
}

android {
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/INDEX.LIST",
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

    implementation(project(":core:data"))
    implementation(project(":core:design-system"))
    implementation(project(":core:i18n"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))

    implementation(project(":feature:home"))
    implementation(project(":feature:charts"))
    implementation(project(":feature:add-transaction"))
    implementation(project(":feature:transactions"))
    implementation(project(":feature:reports"))
    implementation(project(":feature:settings"))
}
