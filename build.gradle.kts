import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    id("org.sonarqube") version "3.3"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
    implementation("mysql:mysql-connector-java:8.0.29")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

sonarqube {
    properties {
        property("sonar.projectKey", "vendro-zoo_racoon")
        property("sonar.organization", "vendro-zoo")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}