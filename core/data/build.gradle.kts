plugins {
    id("budgettracker.android.library")
    id("budgettracker.android.hilt")
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))

    implementation(libs.coroutines.core)
}
