val tbdLibsVersion: String by project
val hikariCPVersion: String by project
val kotliqueryVersion: String by project
val ojdbcVersion = "23.9.0.25.07"
val testcontainersVersion = "2.0.5"

dependencies {
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    implementation(project(":felles"))
    api("com.github.seratch:kotliquery:${kotliqueryVersion}")
    api("com.oracle.database.jdbc:ojdbc11:${ojdbcVersion}")
    testImplementation("com.github.navikt.tbd-libs:rapids-and-rivers-test:$tbdLibsVersion")
    testImplementation("org.testcontainers:testcontainers-oracle-free:$testcontainersVersion")
}
