import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val hikariCPVersion = "4.0.3"
val ojdbcVersion = "21.1.0.0"
val kotliqueryVersion = "1.3.1"
val h2Version = "1.4.200"
val flywayVersion = "7.7.1"

dependencies{
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    implementation("com.oracle.database.jdbc:ojdbc8:$ojdbcVersion")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")

    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("org.flywaydb:flyway-core:$flywayVersion")
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}