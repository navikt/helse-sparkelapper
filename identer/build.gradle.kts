dependencies {
    implementation("org.apache.avro:avro:1.11.0")
    implementation("org.flywaydb:flyway-core:8.5.13")
    implementation("com.github.seratch:kotliquery:1.8.0")
    implementation("org.postgresql:postgresql:42.4.0")
    implementation("com.zaxxer:HikariCP:5.0.1")

    testImplementation("org.testcontainers:postgresql:1.17.2")
}