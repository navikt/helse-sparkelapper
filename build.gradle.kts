import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30"
}

val junitJupiterVersion = "5.7.1"
val rapidsAndRiversVersion = "1.5e3ca6a"
val ktorVersion = "1.5.0" // should be set to same value as rapids and rivers

tasks.create("listProjects") {
    doLast {
        println(subprojects.joinToString { "\"${it.name}\"" })
    }
}

allprojects {
    group = "no.nav.helse.sparkel"
    version = properties["version"] ?: "local-build"

    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        if (!erFellesmodul()) implementation(project(":felles"))

        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "14"
        }

        named<KotlinCompile>("compileTestKotlin") {
            kotlinOptions.jvmTarget = "14"
        }

        withType<Wrapper> {
            gradleVersion = "6.8.3"
        }

        if (!erFellesmodul()) {
            named<Jar>("jar") {
                archiveBaseName.set("app")

                manifest {
                    attributes["Main-Class"] = "${this@allprojects.group}.${this@allprojects.name}.AppKt"
                    attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
                        it.name
                    }
                }

                doLast {
                    configurations.runtimeClasspath.get().forEach {
                        val file = File("$buildDir/libs/${it.name}")
                        if (!file.exists())
                            it.copyTo(file)
                    }
                }
            }
        }
    }
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
    jcenter()
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    ext {
        set("ktorVersion", ktorVersion)
        set("rapidsAndRiversVersion", rapidsAndRiversVersion)
    }

    repositories {
        maven("https://jitpack.io")
        mavenCentral()
        jcenter()
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    }
}

fun Project.erFellesmodul() = name == "felles"