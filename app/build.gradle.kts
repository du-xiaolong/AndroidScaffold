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
    implementation("com.google.android.material:material:1.10.0")
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
}