import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "1.9.23"
    `maven-publish`
}

repositories { mavenCentral() }

kotlin {
    jvmToolchain(21)
    compilerOptions { jvmTarget.set(JvmTarget.JVM_1_8) }
}

java { withSourcesJar(); withJavadocJar() }

dependencies {
    // Tester
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Timers via coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
            groupId = "com.sounddimension"
            artifactId = "kotlin-fsm"
            version = System.getenv("VERSION") ?: "0.1.0" // Set ENV variable VERSION in CI/CD pipeline (GitHub Actions)
            println("Publishing version: $version")
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Sounddimension/kotlin-fsm")
            credentials {
                username = providers.gradleProperty("gpr.user")
                    .orElse(providers.environmentVariable("GPR_USER")).get()
                password = providers.gradleProperty("gpr.key_write_packages")
                    .orElse(providers.environmentVariable("GPR_PACK_PAT")).get()
            }
        }
    }
}