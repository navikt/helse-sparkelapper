import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30"
}

val junitJupiterVersion = "5.7.1"
val rapidsAndRiversVersion = "1.5e3ca6a"
val ktorVersion = "1.5.0" // should be set to same value as rapids and rivers

fun getBuildableProjects(): List<Project> {
    val changedFiles = System.getenv("CHANGED_FILES")?.split(",") ?: emptyList()
    val commonChanges = changedFiles.any {
        it.startsWith("felles/") || it.contains("config/nais.yml") || it.startsWith("build.gradle.kts") || it == ".github/workflows/build.yml"
    }
    if (changedFiles.isEmpty() || commonChanges) return subprojects.toList()
    return subprojects.filter { project -> changedFiles.any { path -> path.contains("${project.name}/") } }
}

fun getDeployableProjects() = getBuildableProjects().filter { File("config", it.name).isDirectory }

tasks.create("buildMatrix") {
    doLast {
        println(""" ${getBuildableProjects().joinToString(prefix = "[", postfix = "]") { "\"${it.name}\"" }} """)
    }
}
tasks.create("deployMatrix") {
    doLast {
        println(""" ${getDeployableProjects().joinToString(prefix = "[", postfix = "]") { "\"${it.name}\"" }} """)
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

    repositories {
        maven("https://jitpack.io")
        maven("https://oss.sonatype.org")
        mavenCentral()
        jcenter()
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

        withType<Test> {
            useJUnitPlatform()
            testLogging {
                events("skipped", "failed")
            }
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

subprojects {
    ext {
        set("ktorVersion", ktorVersion)
        set("rapidsAndRiversVersion", rapidsAndRiversVersion)
    }
}

fun Project.erFellesmodul() = name == "felles"
