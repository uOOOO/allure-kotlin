description = "Allure Kotlin Android Integration"

plugins {
    id("com.android.library")
    `maven-publish`
    signing
}

apply(plugin = "maven-publish")

android {
    namespace = "io.qameta.allure.android"
    compileSdk = Versions.Android.compileSdk
    defaultConfig {
        minSdk = Versions.Android.minSdk

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    api(project(":allure-kotlin-junit4"))
    implementation(kotlin("stdlib-jdk7"))
    implementation("androidx.test.ext:junit:${Versions.Android.Test.junit}")
    implementation("androidx.test:runner:${Versions.Android.Test.runner}")
    implementation("androidx.multidex:multidex:${Versions.Android.multiDex}")
    implementation("androidx.test.uiautomator:uiautomator:${Versions.Android.Test.uiAutomator}")
    compileOnly("org.robolectric:robolectric:${Versions.Android.Test.robolectric}")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["release"])

                pom {
                    name.set(project.name)
                    description.set("Module ${project.name} of Allure Framework.")
                    url.set("https://github.com/allure-framework/allure-kotlin")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("kamildziadek")
                            name.set("Kamil Dziadek")
                            email.set("kamildziadek0@gmail.com")
                        }
                        developer {
                            id.set("viclovsky")
                            name.set("Victor Orlovsky")
                            email.set("viclovsky@gmail.com")
                        }
                    }
                    scm {
                        developerConnection.set("scm:git:git://github.com/allure-framework/allure-kotlin")
                        connection.set("scm:git:git://github.com/allure-framework/allure-kotlin")
                        url.set("https://github.com/allure-framework/allure-kotlin")
                    }
                    issueManagement {
                        system.set("GitHub Issues")
                        url.set("hhttps://github.com/allure-framework/allure-kotlin/issue")
                    }
                }
            }

        }
    }

    signing {
        sign(publishing.publications["maven"])
    }
}
