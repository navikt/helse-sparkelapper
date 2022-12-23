val ktorVersion: String by project
val arrowVersion = "0.12.1"
val rapidsAndRivers = "2022122217191671725962.4c6c2077db70"

dependencies {
    implementation("io.arrow-kt:arrow-core-data:$arrowVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    api("com.github.navikt:rapids-and-rivers:$rapidsAndRivers")
}
