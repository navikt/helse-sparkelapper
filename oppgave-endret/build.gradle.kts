val tbdLibsVersion: String by project

dependencies {
    implementation(project(":felles"))
    testImplementation("com.github.navikt.tbd-libs:rapids-and-rivers-test:$tbdLibsVersion")

    tasks {
        test {
            testLogging {
                events("failed", "standardOut", "standardError")
                info.events("STARTED", "PASSED", "SKIPPED", "FAILED", "STANDARD_OUT", "STANDARD_ERROR")
            }
        }
    }
}
