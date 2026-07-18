plugins {
    id("budgettracker.android.library")
    id("budgettracker.android.compose")
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:design-system"))
    implementation(libs.reorderable)
}
