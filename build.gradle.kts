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
    testImplementation(kotlin("io.kotest:kotest-runner-junit5:5.9.1"))
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