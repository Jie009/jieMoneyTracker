plugins {
    id("budgettracker.android.library")
    id("budgettracker.android.room")
    id("budgettracker.android.hilt")
}

dependencies {
    implementation(project(":core:model"))
}
