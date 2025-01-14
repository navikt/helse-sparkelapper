val testcontainersPostgresqlVersion: String by project
val postgresqlVersion: String by project
val tbdLibsVersion: String by project
val flywayCoreVersion: String by project
val hikariCPVersion: String by project
val exposedVersion = "0.41.1"

dependencies {
    implementation("org.flywaydb:flyway-database-postgresql:$flywayCoreVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation(project(":felles"))

    testImplementation("com.github.navikt.tbd-libs:rapids-and-rivers-test:$tbdLibsVersion")
    testImplementation("com.github.navikt.tbd-libs:postgres-testdatabaser:$tbdLibsVersion")
}

tasks.withType<Test> {
    val parallellDisabled = System.getenv("CI" ) == "true"
    systemProperty("junit.jupiter.execution.parallel.enabled", parallellDisabled.not().toString())
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    systemProperty("junit.jupiter.execution.parallel.config.strategy", "fixed")
    systemProperty("junit.jupiter.execution.parallel.config.fixed.parallelism", "8")
}