import com.fasterxml.jackson.databind.ObjectMapper

val jvmTarget = 21
val gradleWrapperVersion = "8.5"

plugins {
    kotlin("jvm") version "1.9.22"
}

val junitJupiterVersion = "5.10.1"
val rapidsAndRiversVersion = "2024010209171704183456.6d035b91ffb4"
val tbdLibsVersion = "2024.01.13-20.24-cf1b588c"
val ktorVersion = "2.3.7"
val mockkVersion = "1.13.9"
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
        testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    repositories {
        val githubPassword: String? by project
        mavenCentral()
        /* ihht. https://github.com/navikt/utvikling/blob/main/docs/teknisk/Konsumere%20biblioteker%20fra%20Github%20Package%20Registry.md
            så plasseres github-maven-repo (med autentisering) før nav-mirror slik at github actions kan anvende førstnevnte.
            Det er fordi nav-mirroret kjører i Google Cloud og da ville man ellers fått unødvendige utgifter til datatrafikk mellom Google Cloud og GitHub
         */
        maven {
            url = uri("https://maven.pkg.github.com/navikt/maven-release")
            credentials {
                username = "x-access-token"
                password = githubPassword
            }
        }
        maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }

    tasks {
        java {
            toolchain {
                languageVersion = JavaLanguageVersion.of(jvmTarget)
            }
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
    ext.set("tbdLibsVersion", tbdLibsVersion)

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

fun Project.skalLagAppJar() = name !in listOf("felles", "infotrygd")
