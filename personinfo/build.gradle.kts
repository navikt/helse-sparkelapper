val ktorVersion: String by project
val mockkVersion: String by project
val jsonAssertVersion: String by project
val avroVersion: String by project
val tbdLibsVersion: String by project
dependencies {
    implementation("com.github.navikt.tbd-libs:azure-token-client-default:$tbdLibsVersion")
    implementation("com.github.navikt.tbd-libs:speed-client:$tbdLibsVersion")
    implementation("com.github.navikt.tbd-libs:retry:$tbdLibsVersion")
    implementation("org.apache.avro:avro:$avroVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    testImplementation("org.skyscreamer:jsonassert:$jsonAssertVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    implementation(project(":felles"))
}
