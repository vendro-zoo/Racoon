import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.9.0"
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.8.20"
}

group = "it.zoo.vendro"
val fullVersion = project.properties["project.version"] as String
version = fullVersion

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.mysql:mysql-connector-j:8.1.0")

    testImplementation("org.postgresql:postgresql:42.6.0")
    testImplementation("org.testcontainers:testcontainers:1.19.0")
    testImplementation("org.testcontainers:junit-jupiter:1.19.0")
    testImplementation("org.testcontainers:postgresql:1.19.0")
    testImplementation(kotlin("test"))
}

// Function to build source jar file
val sourcesJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    archiveClassifier.set("sources")
    archiveVersion.set(fullVersion)
    from(sourceSets["main"].allSource)
}

// Function to build javadoc jar file
val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn("dokkaJavadoc")
    archiveClassifier.set("javadoc")
    archiveVersion.set(fullVersion)
    from(tasks.javadoc)
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

// Publish all the jar files to the local maven repository
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "it.zoo.vendro"
            artifactId = "racoon"
            version = fullVersion

            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }
}