val tbdLibsVersion: String by project

dependencies {
    implementation(project(":felles"))

    implementation("com.github.navikt.tbd-libs:azure-token-client-default:$tbdLibsVersion")
    implementation("com.github.navikt.tbd-libs:retry:$tbdLibsVersion")
    implementation("com.github.navikt.tbd-libs:spedisjon-client:$tbdLibsVersion")

    testImplementation("com.github.navikt.tbd-libs:rapids-and-rivers-test:$tbdLibsVersion")
}

tasks.withType<Test> {
    val parallellDisabled = System.getenv("CI" ) == "true"
    systemProperty("junit.jupiter.execution.parallel.enabled", parallellDisabled.not().toString())
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    systemProperty("junit.jupiter.execution.parallel.config.strategy", "fixed")
    systemProperty("junit.jupiter.execution.parallel.config.fixed.parallelism", "8")
}
