plugins {
    kotlin("jvm") version "1.9.23"
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.varabyte.kotter:kotter-jvm:1.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "MainKt"
}

kotlin {
    jvmToolchain(8)
}