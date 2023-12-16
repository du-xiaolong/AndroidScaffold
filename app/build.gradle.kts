import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))


android {
    namespace = "com.dxl.androidscaffold"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dxl.androidscaffold"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // 设置支持的SO库架构
            abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    buildFeatures{
        viewBinding = true
        buildConfig = true
        dataBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.2"
    }

    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig =  signingConfigs.getByName("release")
        }

        getByName("debug") {
            isMinifyEnabled = false
            signingConfig =  signingConfigs.getByName("debug")
        }
    }



    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation(project(mapOf("path" to ":scaffold")))

    implementation ("io.github.lucksiege:pictureselector:v3.11.1")


    //百度地图
    //地图组件
    implementation ("com.baidu.lbsyun:BaiduMapSDK_Map:7.5.4")
    //全量定位
    implementation ("com.baidu.lbsyun:BaiduMapSDK_Location_All:9.3.7")
    //搜索
    implementation ("com.baidu.lbsyun:BaiduMapSDK_Search:7.5.4")
    //工具组件
    implementation ("com.baidu.lbsyun:BaiduMapSDK_Util:7.5.4")

    //compose
    val composeBom = platform("androidx.compose:compose-bom:2023.01.00")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.compose.material:material-icons-core")
    // Optional - Add full set of material icons
    implementation("androidx.compose.material:material-icons-extended")
    // Optional - Add window size utils
    implementation("androidx.compose.material3:material3-window-size-class")
    // Optional - Integration with activities
    implementation("androidx.activity:activity-compose:1.8.2")
    // Optional - Integration with ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    // Optional - Integration with LiveData
    implementation("androidx.compose.runtime:runtime-livedata")
    //compose navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

}