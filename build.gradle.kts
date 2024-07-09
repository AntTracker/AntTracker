plugins {
    kotlin("jvm") version "1.9.23"
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"
val exposedVersion = "0.52.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.h2database:h2:2.2.224")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest:kotest-property:5.9.1")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "anttracker.MainKt"
}

kotlin {
    jvmToolchain(8)
}
