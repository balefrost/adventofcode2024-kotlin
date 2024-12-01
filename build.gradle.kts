plugins {
    kotlin("jvm") version "2.1.0"
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.hamcrest:hamcrest:3.0")
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}