plugins {
    kotlin("jvm") version "2.1.0"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable-jvm:0.3.8")
    testImplementation(kotlin("test"))
    testImplementation("org.hamcrest:hamcrest:3.0")
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}