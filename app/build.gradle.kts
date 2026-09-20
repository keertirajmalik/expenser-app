plugins {
    alias(libs.plugins.android.application)
    // AGP 9 has built-in Kotlin; the kotlin.android plugin is no longer applied.
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Release version comes from the git tag, passed by CI as -PappVersion=1.2.3.
// Local builds fall back to a 0.0.1 dev version.
val appVersion = (findProperty("appVersion") as String? ?: "0.0.1").removePrefix("v")
val appVersionParts = appVersion.split(".").map { it.toIntOrNull() ?: 0 }
val appVersionCode = (
    appVersionParts.getOrElse(0) { 0 } * 1_000_000 +
        appVersionParts.getOrElse(1) { 0 } * 1_000 +
        appVersionParts.getOrElse(2) { 0 }
    ).coerceAtLeast(1)

// Signing credentials are injected by CI. They are absent on a dev machine, so
// local release builds stay unsigned rather than failing.
val keystorePath: String? = System.getenv("KEYSTORE_PATH")

android {
    namespace = "com.expenser.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.expenser.app"
        minSdk = 35
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersion
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}

ksp {
    // Room writes schemas/<version>.json here; committed so migrations can diff against it.
    arg("room.schemaLocation", "$projectDir/schemas")
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    debugImplementation(libs.androidx.ui.tooling)
}
