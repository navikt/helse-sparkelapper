val ktorVersion: String by project
val tbdLibsVersion: String by project
val arrowVersion = "0.12.1"
dependencies {
    implementation("com.github.navikt.tbd-libs:azure-token-client:$tbdLibsVersion")
    implementation("io.arrow-kt:arrow-core-data:$arrowVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation(project(":felles"))
}
