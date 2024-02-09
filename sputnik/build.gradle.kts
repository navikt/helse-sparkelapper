val ktorVersion: String by project
val jsonAssertVersion: String by project
//val tbdLibsVersion: String by project
val tbdLibsVersion = "2024.02.09-10.44-24d5802f"

dependencies {
    implementation("com.github.navikt.tbd-libs:azure-token-client-default:$tbdLibsVersion")
    implementation("com.github.navikt.tbd-libs:retry:$tbdLibsVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation(project(":felles"))
    testImplementation("org.skyscreamer:jsonassert:$jsonAssertVersion")
}
