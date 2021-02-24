dependencies {
    testImplementation("io.mockk:mockk:1.10.0")
    testImplementation("com.github.tomakehurst:wiremock:2.27.1") {
        exclude(group = "junit")
        exclude("com.github.jknack.handlebars.java")
    }
}
