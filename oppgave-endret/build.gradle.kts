val rapidsAndRiversVersion: String by project

dependencies {
    implementation(project(":felles"))
    testImplementation("com.github.navikt.rapids-and-rivers:rapids-and-rivers-test:$rapidsAndRiversVersion")

    tasks {
        test {
            testLogging {
                events("failed", "standardOut", "standardError")
                info.events("STARTED", "PASSED", "SKIPPED", "FAILED", "STANDARD_OUT", "STANDARD_ERROR")
            }
        }
    }
}
