plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.dxl.scaffold"
    compileSdk = 34

    defaultConfig {
        minSdk = 22

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.10-1.0.13")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    api("com.github.li-xiaojun:XPopup:2.9.19")
    //腾讯MMKV
    implementation("com.tencent:mmkv:1.2.16")
    //fastJson
    api("com.alibaba:fastjson:1.2.79")

    // ViewModel
    api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // LiveData
    api("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    api("androidx.collection:collection-ktx:1.3.0")
    api("androidx.fragment:fragment-ktx:1.6.1")
    api("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    api("com.squareup.retrofit2:retrofit:2.9.0")
    api("com.blankj:utilcodex:1.31.1")

    //rxHttp
    api("com.github.liujingxing.rxhttp:rxhttp:3.2.1")
    //添加okhttp依赖
    api("com.squareup.okhttp3:okhttp:4.11.0")
    //noinspection KaptUsageInsteadOfKsp
    kapt("com.github.liujingxing.rxhttp:rxhttp-compiler:3.2.1")

    // 图片压缩 (按需引入)
    api ("io.github.lucksiege:compress:v3.0.9")

    api("com.github.bumptech.glide:glide:4.15.1")

    api("io.github.cymchad:BaseRecyclerViewAdapterHelper:3.0.14")


}