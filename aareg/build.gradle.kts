val ktorVersion: String by project

dependencies {
    implementation("io.arrow-kt:arrow-core-data:0.12.1")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")

    testImplementation("io.mockk:mockk:1.12.0")
}
