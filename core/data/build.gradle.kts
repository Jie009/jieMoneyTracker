plugins {
    id("budgettracker.android.library")
    id("budgettracker.android.hilt")
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))

    implementation(libs.coroutines.core)
    implementation(libs.androidx.room.runtime)
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.drive)
    implementation(libs.google.http.client.android)
    implementation(libs.google.http.client.gson)
    implementation(libs.play.services.auth)
}
