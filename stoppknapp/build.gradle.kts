val ktorVersion: String by project
val postgresqlVersion: String by project
val flywayCoreVersion: String by project
val hikariCPVersion: String by project
val kotliqueryVersion: String by project
val testcontainersPostgresqlVersion: String by project

dependencies {
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    implementation("org.flywaydb:flyway-core:$flywayCoreVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayCoreVersion")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")

    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    implementation(project(":felles"))

    testImplementation("org.testcontainers:postgresql:$testcontainersPostgresqlVersion")
}
