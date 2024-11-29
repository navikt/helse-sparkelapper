val ktorVersion: String by project
val tbdLibsVersion: String by project

dependencies {
    implementation("com.github.navikt.tbd-libs:naisful-app:$tbdLibsVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation(project(":felles"))

    testImplementation("com.github.navikt.tbd-libs:rapids-and-rivers-test:$tbdLibsVersion")
}
