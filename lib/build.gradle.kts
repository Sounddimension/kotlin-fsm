plugins {
    kotlin("jvm") version "1.9.23"
}

repositories { mavenCentral() }

kotlin {
    jvmToolchain(21) // eller 17 om du föredrar
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