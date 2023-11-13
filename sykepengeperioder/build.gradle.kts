import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val hikariCPVersion = "5.0.1"
val h2Version = "2.0.206"
val flywayVersion = "8.4.1"

dependencies{
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")

    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation(project(":infotrygd"))

}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}
