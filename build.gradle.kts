import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30"
}

val junitJupiterVersion = "5.7.1"
val rapidsAndRiversVersion = "1.5e3ca6a"
val ktorVersion = "1.5.0" // should be set to same value as rapids and rivers

buildscript {
    repositories { mavenCentral() }
    dependencies { "classpath"(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.12.0") }
}

val mapper = ObjectMapper()

fun getBuildableProjects(): List<Project> {
    val changedFiles = System.getenv("CHANGED_FILES")?.split(",") ?: emptyList()
    val commonChanges = changedFiles.any {
        it.startsWith("felles/") || it.contains("config/nais.yml") || it.startsWith("build.gradle.kts") || it == ".github/workflows/build.yml"
    }
    if (changedFiles.isEmpty() || commonChanges) return subprojects.toList()
    return subprojects.filter { project -> changedFiles.any { path -> path.contains("${project.name}/") } }
}

fun getDeployableProjects() = getBuildableProjects()
    .filter { project -> File("config", project.name).isDirectory }

tasks.create("buildMatrix") {
    doLast {
        println(mapper.writeValueAsString(mapOf(
            "project" to getBuildableProjects().map { it.name }
        )))
    }
}
tasks.create("deployMatrix") {
    doLast {
        // map of cluster to list of apps
        val deployableProjects = getDeployableProjects().map { it.name }
        val environments = deployableProjects
            .map { project ->
                project to (File("config", project)
                    .listFiles()
                    ?.filter { it.isFile && it.name.endsWith(".yml") }
                    ?.map { it.name.removeSuffix(".yml") }
                    ?: emptyList())
            }.toMap()

        val clusters = environments.flatMap { it.value }.distinct()
        val exclusions = environments
            .mapValues { (app, configs) ->
                clusters.filterNot { it in configs }
            }
            .filterValues { it.isNotEmpty() }
            .flatMap { (app, clusters) ->
                clusters.map { cluster -> mapOf(
                    "app" to app,
                    "cluster" to cluster
                )}
            }

        println(mapper.writeValueAsString(mapOf(
            "cluster" to clusters,
            "project" to deployableProjects,
            "exclude" to exclusions
        )))
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
    dependencies {
        testImplementation("io.mockk:mockk:1.10.0")
        testImplementation("com.github.tomakehurst:wiremock:2.27.1") {
            exclude(group = "junit")
            exclude("com.github.jknack.handlebars.java")
        }
    }
}

fun Project.erFellesmodul() = name == "felles"
