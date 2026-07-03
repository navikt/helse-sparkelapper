val ktorVersion: String by project
val tbdLibsVersion: String by project
val rapidsAndRiversVersion: String by project
val jacksonVersion: String by project

dependencies {
    implementation("com.github.navikt.tbd-libs:naisful-app:$tbdLibsVersion")
    implementation("com.github.navikt.rapids-and-rivers:rapids-and-rivers-impl:$rapidsAndRiversVersion")
    implementation("io.ktor:ktor-serialization-jackson3:$ktorVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.22.0")
    implementation("tools.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation(project(":felles"))

    testImplementation("com.github.navikt.rapids-and-rivers:rapids-and-rivers-test:$rapidsAndRiversVersion")
}
