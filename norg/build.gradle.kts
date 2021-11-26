val ktorVersion: String by project
val cxfVersion: String by project

dependencies {
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-logging-jvm:$ktorVersion")

    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
}
