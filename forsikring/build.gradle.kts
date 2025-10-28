val tbdLibsVersion: String by project
val hikariCPVersion: String by project
val flywayCoreVersion: String by project
val h2Version = "2.2.220"
val kotliqueryVersion: String by project
val ojdbcVersion = "23.9.0.25.07"

dependencies {
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    implementation(project(":felles"))
    api("com.github.seratch:kotliquery:${kotliqueryVersion}")
    api("com.oracle.database.jdbc:ojdbc11:${ojdbcVersion}")
    testImplementation("com.github.navikt.tbd-libs:rapids-and-rivers-test:$tbdLibsVersion")
    testImplementation("org.flywaydb:flyway-database-postgresql:${flywayCoreVersion}")
    testImplementation("com.h2database:h2:${h2Version}")
}
