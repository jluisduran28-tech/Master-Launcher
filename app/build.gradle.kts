plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.example.tvlauncher"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.tvlauncher"
        minSdk = 21          // Android TV 5.0+
        targetSdk = 35       // verifica el mínimo vigente de Google Play antes de publicar
        versionCode = 9
        versionName = "4.7"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    // La interfaz vive en ../web (una sola fuente para Android, Tizen y webOS)
    sourceSets["main"].assets.srcDirs("../web")
}
dependencies {
    implementation("androidx.webkit:webkit:1.12.1")
}
