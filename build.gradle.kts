import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.3.50"
    id ("org.jetbrains.kotlin.plugin.serialization") version "1.3.50"
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

application {
    group = "com.nubank"
    version = "1.0"
    mainClassName = "app.ApplicationKt"
}

// config JVM target to 1.8 for kotlin compilation tasks
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {

    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0")
    implementation("com.beust:klaxon:5.0.1")
    implementation(kotlin("reflect:1.3.0"))
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.11.2")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.0")
    testImplementation("org.hamcrest:hamcrest:2.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<ShadowJar>() {
    manifest {
        attributes["Main-Class"] = "app.ApplicationKt"
    }
}



