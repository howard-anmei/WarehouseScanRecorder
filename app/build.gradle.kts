/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt.gradle)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.anmei.warehouseputaway"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.anmei.warehouseputaway"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "com.anmei.warehouseputaway.HiltTestRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        aidl = false
        buildConfig = false
        shaders = false
    }

    packaging {
        resources {
            excludes +=
                "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

ksp {
    arg(
        "room.schemaLocation",
        "$projectDir/schemas"
    )
}

dependencies {

    // ========================================
    // Compose BOM
    // ========================================

    val composeBom =
        platform(libs.androidx.compose.bom)

    implementation(composeBom)

    androidTestImplementation(composeBom)


    // ========================================
    // Core Android
    // ========================================

    implementation(libs.androidx.core.ktx)

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )

    implementation(
        libs.androidx.activity.compose
    )


    // ========================================
    // Hilt
    // ========================================

    implementation(
        libs.hilt.android
    )

    ksp(
        libs.hilt.compiler
    )


    // ========================================
    // Hilt Navigation Compose
    // ========================================

    implementation(
        "androidx.hilt:hilt-navigation-compose:1.2.0"
    )


    // ========================================
    // Hilt WorkManager
    // ========================================

    implementation(
        libs.androidx.hilt.work
    )

    ksp(
        libs.androidx.hilt.compiler
    )

    // ========================================
    // Hilt Tests
    // ========================================

    androidTestImplementation(
        libs.hilt.android.testing
    )

    // DataStore
    implementation(
        "androidx.datastore:datastore-preferences:1.1.7"
    )

    kspAndroidTest(
        libs.hilt.android.compiler
    )

    testImplementation(
        libs.hilt.android.testing
    )

    kspTest(
        libs.hilt.android.compiler
    )


    // ========================================
    // Lifecycle
    // ========================================

    implementation(
        libs.androidx.lifecycle.runtime.compose
    )

    implementation(
        libs.androidx.lifecycle.viewmodel.compose
    )


    // ========================================
    // Room
    // ========================================

    implementation(
        libs.androidx.room.runtime
    )

    implementation(
        libs.androidx.room.ktx
    )

    ksp(
        libs.androidx.room.compiler
    )


    // ========================================
    // Compose UI
    // ========================================

    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    implementation(
        libs.androidx.compose.material3
    )


    // ========================================
    // Compose Tooling
    // ========================================

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )


    // ========================================
    // Compose Instrumented Tests
    // ========================================

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )


    // ========================================
    // Local Tests
    // ========================================

    testImplementation(
        libs.junit
    )

    testImplementation(
        libs.kotlinx.coroutines.test
    )


    // ========================================
    // Android Instrumented Tests
    // ========================================

    androidTestImplementation(
        libs.androidx.test.core
    )

    androidTestImplementation(
        libs.androidx.test.ext.junit
    )

    androidTestImplementation(
        libs.androidx.test.runner
    )


    // ========================================
    // Navigation 3
    // ========================================

    implementation(
        libs.androidx.navigation3.ui
    )

    implementation(
        libs.androidx.navigation3.runtime
    )

    implementation(
        libs.androidx.lifecycle.viewmodel.navigation3
    )


    // ========================================
    // WorkManager
    // ========================================

    implementation(
        libs.androidx.work.runtime.ktx
    )
}