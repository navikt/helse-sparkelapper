val tbdLibsVersion: String by project
dependencies {
    implementation("com.github.navikt.tbd-libs:azure-token-client-default:$tbdLibsVersion")
    implementation("io.ktor:ktor-client-apache:2.2.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.2.3")
    implementation("io.ktor:ktor-serialization-jackson:2.2.3")
    implementation("io.ktor:ktor-client-logging:2.2.3")
    implementation(project(":felles"))
}