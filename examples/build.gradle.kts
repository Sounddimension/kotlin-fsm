plugins {
    kotlin("jvm") version "1.9.23"
    application
}

repositories { mavenCentral() }
kotlin { jvmToolchain(21) }

dependencies {
    implementation(project(":lib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

application {
    // valfri default-main om du vill:
    // mainClass.set("examples.SmartHomeMainKt")
}

// Körbara tasks
tasks.register<JavaExec>("runSmartHome") {
    group = "application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("examples.SmartHomeMainKt")
}
tasks.register<JavaExec>("runSpeaker") {
    group = "application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("examples.SpeakerMainKt")
}
tasks.register<JavaExec>("runTrafficLight") {
    group = "application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("examples.TrafficLightMainKt")
}
tasks.register<JavaExec>("runDRY") {
    group = "application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("examples.DRYKt")
}