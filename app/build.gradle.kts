plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

android {
    namespace = "com.aizej.easyrlc"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aizej.easyrlc"
        minSdk = 32
        targetSdk = 35
        versionCode = 7
        versionName = "1.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    packaging {
        resources {
            pickFirsts += "graphml.xsd"
            pickFirsts += "xlink.xsd"
            pickFirsts += "viz.xsd"
            pickFirsts += "META-INF/DEPENDENCIES"
            pickFirsts += "gexf.xsd"
        }
    }
}

dependencies {
    // Core libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity) // This should resolve to androidx.activity:activity

    // Compose UI
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)

    // Activity Compose
    implementation(libs.androidx.activity.compose)

    // Compose Compiler
    implementation(libs.androidx.compiler) // or latest version

    implementation("net.objecthunter:exp4j:0.4.8")
    implementation("com.tecacet:komplex:1.0.0")


    //charts


    //charts 2
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m2)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.core)


    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

