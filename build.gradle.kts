import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    val kotlinVersion = "1.9.0"
    kotlin("jvm") version kotlinVersion
    id("application")
    id("com.diffplug.spotless") version "6.20.0"
}

group = "blippy"

repositories {
    mavenCentral()
}

application {
    mainClass.set("blippy.Blippy")
}

kotlin {
    jvmToolchain(17)
}

val logbackVersion = "1.4.8"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("com.dampcake:bencode:1.4.1")
    implementation("io.netty:netty-all:4.1.96.Final")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

configure<SpotlessExtension> {
    kotlin {
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_standard_no-wildcard-imports" to "disabled",
            ),
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
