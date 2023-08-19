import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val vers: String by project

plugins {
    kotlin("jvm") version "1.6.20"
    id("maven-publish")  // Used to publish to the local maven repository
    id("org.jetbrains.dokka") version "1.6.21"  // Used to generate the API documentation
    id("org.sonarqube") version "3.3"  // Used to perform cloud-based analysis
}

group = "it.zoo.vendro"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
    testImplementation("mysql:mysql-connector-java:8.0.29")
    testImplementation("org.postgresql:postgresql:42.6.0")

    dokkaJavadocPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.6.21")  // Used to generate the API documentation as javadoc

    testImplementation(kotlin("test"))
}

// Function to build source jar file
val sourcesJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    archiveClassifier.set("sources")
    archiveVersion.set(vers)
    from(sourceSets["main"].allSource)
}

// Function to build javadoc jar file
val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn("dokkaJavadoc")
    archiveClassifier.set("javadoc")
    archiveVersion.set(vers)
    from(tasks.javadoc)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

sonarqube {
    properties {
        property("sonar.projectKey", "vendro-zoo_racoon")
        property("sonar.organization", "vendro-zoo")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

// Publish all the jar files to the local maven repository
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "it.zoo.vendro"
            artifactId = "racoon"
            version = vers

            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }
}