val tbdLibsVersion: String by project
dependencies {
    implementation("com.github.navikt.tbd-libs:azure-token-client-default:$tbdLibsVersion")
    implementation("org.apache.avro:avro:1.11.0")
    implementation("io.ktor:ktor-client-apache:2.2.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.2.3")
    implementation("io.ktor:ktor-serialization-jackson:2.2.3")
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
    testImplementation("io.mockk:mockk:1.12.8")
    implementation(project(":felles"))
}
