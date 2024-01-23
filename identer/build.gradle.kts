
val avroVersion: String by project
val kotliqueryVersion: String by project
val testcontainersPostgresqlVersion: String by project
val postgresqlVersion: String by project
val flyAwayCoreVersion: String by project
val hikariCPVersion: String by project

dependencies {
    implementation("org.apache.avro:avro:$avroVersion")
    implementation("org.flywaydb:flyway-core:$flyAwayCoreVersion")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    implementation(project(":felles"))

    testImplementation("org.testcontainers:postgresql:$testcontainersPostgresqlVersion")
}