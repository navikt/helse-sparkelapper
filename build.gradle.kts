import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val jvmTarget = "17"

plugins {
    kotlin("jvm") version "1.9.10"
}

val gradleWrapperVersion = "8.1.1"
val junitJupiterVersion = "5.8.2"
val rapidsAndRiversVersion = "2023093008351696055717.ffdec6aede3d"
val ktorVersion = "2.1.1"
val cxfVersion = "3.5.3"
val mockkVersion = "1.12.0"
val wiremockVersion = "2.27.2"

buildscript {
    repositories { mavenCentral() }
    dependencies { "classpath"(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.13.2.2") }
}

val mapper = ObjectMapper()

fun getBuildableProjects(): List<Project> {
    val changedFiles = System.getenv("CHANGED_FILES")?.split(",") ?: emptyList()
    val commonChanges = changedFiles.any {
        it.startsWith("felles/") || it.contains("config/nais.yml")
                || it.startsWith("build.gradle.kts")
                || it == ".github/workflows/build.yml"
                || it == "Dockerfile"
                || it == "settings.gradle.kts"
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
                    ?.filterNot { it.name.contains("aiven") }
                    ?.map { it.name.removeSuffix(".yml") }
                    ?: emptyList())
            }.toMap()

        val clusters = environments.flatMap { it.value }.distinct()
        val exclusions = environments
            .mapValues { (_, configs) ->
                clusters.filterNot { it in configs }
            }
            .filterValues { it.isNotEmpty() }
            .flatMap { (app, clusters) ->
                clusters.map { cluster -> mapOf(
                    "project" to app,
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
        val githubPassword: String by project
        maven("https://packages.confluent.io/maven/")
        maven("https://oss.sonatype.org")
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/navikt/*")
            credentials {
                username = "x-access-token"
                password = githubPassword
            }
        }
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = jvmTarget
        }

        named<KotlinCompile>("compileTestKotlin") {
            kotlinOptions.jvmTarget = jvmTarget
        }

        withType<Wrapper> {
            gradleVersion = gradleWrapperVersion
        }

        withType<Test> {
            useJUnitPlatform()
            testLogging {
                events("skipped", "failed")
            }
        }
    }
}

subprojects {
    ext.set("ktorVersion", ktorVersion)
    ext.set("rapidsAndRiversVersion", rapidsAndRiversVersion)
    ext.set("cxfVersion", cxfVersion)

    dependencies {
        testImplementation("io.mockk:mockk:$mockkVersion")
        testImplementation("com.github.tomakehurst:wiremock:$wiremockVersion") {
            exclude(group = "junit")
            exclude("com.github.jknack.handlebars.java")
        }
        testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion")
    }
    tasks {
        if (project.skalLagAppJar()) {
            named<Jar>("jar") {
                archiveBaseName.set("app")

                val mainClass = project.mainClass()

                doLast {
                    val mainClassFound = this.project.sourceSets.findByName("main")?.let {
                        it.output.classesDirs.asFileTree.any { it.path.contains(mainClass.replace(".", File.separator)) }
                    } ?: false

                    if (!mainClassFound) throw RuntimeException("Kunne ikke finne main class: $mainClass")
                }

                manifest {
                    attributes["Main-Class"] = mainClass
                    attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") { it.name }
                }

                doLast {
                    configurations.runtimeClasspath.get().forEach {
                        val file = File("${layout.buildDirectory.get()}/libs/${it.name}")
                        if (!file.exists())
                            it.copyTo(file)
                    }
                }
            }
        }
    }
}

fun Project.mainClass() =
    "$group.${name.replace("-", "")}.AppKt"

fun Project.erFellesmodul() = name == "felles"
fun Project.skalLagAppJar() = name !in listOf("felles", "abakus")