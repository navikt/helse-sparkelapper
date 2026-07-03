val tbdLibsVersion: String by project
val rapidsAndRiversVersion: String by project
val hikariCPVersion: String by project
val flywayCoreVersion: String by project
val h2Version = "2.2.220"

dependencies {
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    implementation(project(":felles"))
    implementation(project(":infotrygd"))

    testImplementation("com.github.navikt.rapids-and-rivers:rapids-and-rivers-test:$rapidsAndRiversVersion")
    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("org.flywaydb:flyway-database-postgresql:$flywayCoreVersion")
}
