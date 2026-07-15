plugins {
    id("budgettracker.android.feature")
    id("budgettracker.android.compose")
    id("budgettracker.android.hilt")
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
