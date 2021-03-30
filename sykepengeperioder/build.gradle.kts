import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val h2Version = "1.4.200"
val flywayVersion = "7.7.1"
val kotliqueryVersion = "1.3.1"

dependencies{
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")

    implementation("com.h2database:h2:$h2Version")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}