val tbdLibsVersion: String by project

dependencies {
    implementation("com.github.navikt.tbd-libs:azure-token-client-default:$tbdLibsVersion")
    implementation("com.github.navikt.tbd-libs:minimal-soap-client:$tbdLibsVersion")
    implementation(project(":felles"))

    testImplementation("com.github.navikt.tbd-libs:mock-http-client:$tbdLibsVersion")
}
