val testcontainersPostgresqlVersion: String by project
val postgresqlVersion: String by project
val flywayCoreVersion: String by project
val hikariCPVersion: String by project
val exposedVersion = "0.41.1"

dependencies {
    implementation("org.flywaydb:flyway-core:$flywayCoreVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation(project(":felles"))

    testImplementation("org.testcontainers:postgresql:$testcontainersPostgresqlVersion")
}
