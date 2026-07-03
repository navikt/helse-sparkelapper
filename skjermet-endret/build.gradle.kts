val rapidsAndRiversVersion: String by project

dependencies {
    implementation(project(":felles"))

    testImplementation("com.github.navikt.rapids-and-rivers:rapids-and-rivers-test:$rapidsAndRiversVersion")
}
