val tbdLibsVersion: String by project
val hikariCPVersion: String by project
val flywayCoreVersion: String by project
val h2Version = "2.2.220"

dependencies {
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    implementation(project(":felles"))

    testImplementation("com.github.navikt.tbd-libs:rapids-and-rivers-test:$tbdLibsVersion")
}
