pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SilentSync"
include(":app")
include(":dndCalendar:impl")
include(":feature:dndCalendar")
include(":utility")
include(":dndCalendar:api")
include(":testUtil")
include(":playReview:api")
include(":playReview:impl")
