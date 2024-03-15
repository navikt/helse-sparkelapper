dependencies {
    implementation(project(":felles"))
    tasks {
        test {
            testLogging {
                events("failed", "standardOut", "standardError")
                info.events("STARTED", "PASSED", "SKIPPED", "FAILED", "STANDARD_OUT", "STANDARD_ERROR")
            }
        }
    }
}
