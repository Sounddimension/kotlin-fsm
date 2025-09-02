import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    kotlin("jvm") version "1.9.23"
    `maven-publish`
}

repositories { mavenCentral() }

kotlin {
    jvmToolchain(17)
    compilerOptions { jvmTarget.set(JvmTarget.JVM_1_8) }
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

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
            version = System.getenv("VERSION") ?: "0.1.5" // Set ENV variable VERSION in CI/CD pipeline (GitHub Actions)
            println("Publishing version: $version")
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Sounddimension/kotlin-fsm")
            credentials {
                username = providers.environmentVariable("GPR_USER")
                    .orElse(providers.environmentVariable("GITHUB_ACTOR"))
                    .orElse("unknown").get()
                password = providers.environmentVariable("GPR_PAT")
                    .orElse(providers.environmentVariable("GITHUB_TOKEN"))
                    .orElse("missing").get()
            }
        }
    }
}