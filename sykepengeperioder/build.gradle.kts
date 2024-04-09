import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val hikariCPVersion: String by project
val flywayCoreVersion: String by project
val h2Version = "2.2.220"

dependencies {
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    implementation(project(":felles"))
    implementation(project(":infotrygd"))

    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("org.flywaydb:flyway-core:$flywayCoreVersion")
    testImplementation("org.flywaydb:flyway-database-postgresql:$flywayCoreVersion")
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}
