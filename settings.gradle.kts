pluginManagement {
    includeBuild("build-logic")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BudgetTracker"

include(":app")

include(":core:model")
include(":core:common")
include(":core:database")
include(":core:datastore")
include(":core:data")
include(":core:domain")
include(":core:design-system")
include(":core:ui")
include(":core:i18n")

include(":feature:home")
include(":feature:charts")
include(":feature:add-transaction")
include(":feature:transactions")
include(":feature:reports")
include(":feature:settings")
