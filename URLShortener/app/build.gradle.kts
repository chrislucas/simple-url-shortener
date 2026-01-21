import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("jacoco")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    namespace = "com.br.urlshortener"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.br.urlshortener"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BASE_URL", "\"https://url-shortener-server.onrender.com\"")

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            // For AGP 8.0 and later:
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
        }
    }

    /*
        Java versions in Android builds
        https://developer.android.com/build/jdks
     */
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }


    buildFeatures {
        compose = true
        buildConfig = true
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    // Add KTX extensions for lifecycle-aware components (optional but recommended)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")

    // Retrofit main library
    implementation("com.squareup.retrofit2:retrofit:3.0.0")

    // A converter library (e.g., Gson) to process JSON data
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // Optional: OkHttp logging interceptor for debugging network requests
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")

    // Optional: Kotlin Coroutines for asynchronous operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation(libs.androidx.compose.foundation.layout)
    implementation("androidx.navigation:navigation-compose:2.9.6")
    implementation("androidx.compose.material:material-icons-core:1.7.8")

    // Splash Screen API
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.androidx.compose.runtime)
    testImplementation(libs.junit.junit)

    // https://github.com/mrmans0n/compose-rules
    // https://mrmans0n.github.io/compose-rules/ktlint/
    detektPlugins("io.nlopez.compose.rules:detekt:0.5.3") // Use the latest version
    detektPlugins("dev.detekt:detekt-rules-ktlint-wrapper:2.0.0-alpha.1")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")

    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation("io.mockk:mockk:1.14.7")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation("io.mockk:mockk-android:1.14.7")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.navigation:navigation-testing:2.9.6")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("org.robolectric:robolectric:4.16.1")
}

tasks.register<JacocoReport>("jacocoFullCoverageReportAllModules") {
    group = "Reports"
    description = "Generate JaCoCo coverage reports (Unit + Instrumented) for all modules"

    dependsOn("testDebugUnitTest")

    val fileFilter = listOf(
        // Android-specific generated files
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/resources/**",
        "**/values/**",

        // Test files
        "**/*Test*.*",
        "**/*Test$*.*",
        "**/androidTest/**",
        "**/test/**",

        // Hilt/Dagger-generated code
        "**/hilt_aggregated_deps/**",
        "**/dagger/hilt/internal/**",
        "**/dagger/hilt/android/internal/**",
        "**/*_MembersInjector.class",
        "**/Dagger*Component.class",
        "**/*Module_*Factory.class",
        "**/*_Factory.class",
        "**/*_Provide*Factory.class",
        "**/*_Impl.class",

        // Kotlin-generated classes
        "**/*\$Lambda$*.*",
        "**/*\$inlined$*.*",
        "**/*\$*.*", // anonymous classes and lambdas
        "**/Companion.class",

        // Navigation safe args (generated)
        "**/*Directions*.class",
        "**/*Args.class",

        // Jetpack Compose compiler-generated
        "**/*Preview*.*",
        "**/*ComposableSingletons*.*",

        // Room and other annotation processors
        "**/*_Impl.class",
        "**/*Serializer.class", // For Moshi, Retrofit, etc.

        // Miscellaneous
        "android/**/*.*",

        // Project-specific exclusions
        "**/di/**",
        "**/state/**",
        "**/mapper/**",
        "**/domain/**"
    )

    val javaClasses = mutableListOf<FileTree>()
    val kotlinClasses = mutableListOf<FileTree>()
    val javaSrc = mutableListOf<String>()
    val kotlinSrc = mutableListOf<String>()
    val execution = mutableListOf<FileTree>()

    val buildDirectory = rootProject.layout.buildDirectory

    println("Generating JaCoCo full coverage report for all modules... $buildDirectory")

    rootProject.subprojects.forEach { proj ->
        proj.tasks.findByName("testDebugUnitTest")?.let { dependsOn(it) }
        proj.tasks.findByName("connectedDebugAndroidTest")?.let { dependsOn(it) }

        javaClasses.add(proj.fileTree("$buildDirectory/intermediates/javac/debug") {
            exclude(fileFilter)
        })

        kotlinClasses.add(proj.fileTree("$buildDirectory/tmp/kotlin-classes/debug") {
            exclude(fileFilter)
        })

        javaSrc.add("${proj.projectDir}/src/main/java")
        kotlinSrc.add("${proj.projectDir}/src/main/kotlin")

        execution.add(proj.fileTree("$buildDirectory") {
            include(
                "jacoco/testDebugUnitTest.exec", // Unit test
                "outputs/code_coverage/debugAndroidTest/connected/**/*.ec" // UI test
            )
        })
    }

    sourceDirectories.setFrom(files(javaSrc + kotlinSrc))
    classDirectories.setFrom(files(javaClasses + kotlinClasses))
    executionData.setFrom(files(execution))

    reports {
        xml.required.set(true)
        html.required.set(true)

        xml.outputLocation.set(
            file("$buildDirectory/reports/jacoco/xml")
        )

        html.outputLocation.set(
            file("$buildDirectory/reports/jacoco/html")
        )
    }

    doLast {
        println("✅ Combined coverage report generated at:")
        println("📄 file://${reports.html.outputLocation.get()}/index.html")
    }
}

tasks.register("runAllCoverageAndReport") {
    group = "Verification"
    description = "Runs unit + UI tests across all modules and generates a full Jacoco report"

    val testTaskPaths = mutableListOf<String>()

    rootProject.subprojects.forEach { proj ->
        proj.tasks.matching { it.name == "testDebugUnitTest" }.forEach {
            testTaskPaths.add(it.path)
        }
        proj.tasks.matching { it.name == "createDebugCoverageReport" }.forEach {
            testTaskPaths.add(it.path)
        }
    }

    dependsOn(testTaskPaths)
    dependsOn("jacocoFullCoverageReportAllModules")

    doFirst {
        println("Running test tasks:")
        testTaskPaths.forEach { println(" - $it") }
    }
}

/*
    https://detekt.dev/docs/intro
    https://github.com/detekt/detekt
 */
detekt {
    toolVersion = "1.23.8"
    autoCorrect = true
    parallel = true
    config.setFrom(file("${rootProject.layout.projectDirectory}/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

// Kotlin DSL
tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}