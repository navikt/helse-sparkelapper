
val avroVersion = "1.11.0"
val flyAwayCoreVersion = "8.5.13"
val kotliqueryVersion = "1.8.0"
val postgresqlVersion = "42.4.0"
val hikariCPVersion = "5.0.1"
val testcontainersOostgresqlVersion = "1.17.2"

dependencies {
    implementation("org.apache.avro:avro:$avroVersion")
    implementation("org.flywaydb:flyway-core:$flyAwayCoreVersion")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")

    testImplementation("org.testcontainers:postgresql:$testcontainersOostgresqlVersion")
}