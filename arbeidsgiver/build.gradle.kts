val flyAwayCoreVersion = "9.19.4"
val postgresqlVersion = "42.6.0"
val hikariCPVersion = "5.0.1"
val testcontainersPostgresqlVersion = "1.18.3"
val exposedVersion = "0.41.1"

dependencies {
    implementation("org.flywaydb:flyway-core:$flyAwayCoreVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation(project(":felles"))

    testImplementation("org.testcontainers:postgresql:$testcontainersPostgresqlVersion")
}