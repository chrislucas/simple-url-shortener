// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("io.gitlab.arturbosch.detekt") version("1.23.6")
    id("org.jlleitschuh.gradle.ktlint") version("12.1.0")
}

rootProject.extra["jacocoVersion"] = "0.8.11"