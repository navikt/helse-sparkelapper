val ktorVersion: String by project
val cxfVersion: String by project

val tbdLibsVersion: String by project
dependencies {
    implementation("com.github.navikt.tbd-libs:azure-token-client:$tbdLibsVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation(project(":felles"))

    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
}
