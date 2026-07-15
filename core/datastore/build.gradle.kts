plugins {
    id("budgettracker.android.library")
    id("budgettracker.android.hilt")
}

dependencies {
    implementation(project(":core:model"))
}
