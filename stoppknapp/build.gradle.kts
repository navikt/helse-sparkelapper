val ktorVersion: String by project
dependencies {
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation(project(":felles"))
}
