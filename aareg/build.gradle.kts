val ktorVersion: String by project
val arrowVersion = "0.12.1"

dependencies {
    implementation("io.arrow-kt:arrow-core-data:$arrowVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
}
