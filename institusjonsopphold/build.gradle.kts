val tbdLibsVersion: String by project
dependencies {
    implementation("com.github.navikt.tbd-libs:azure-token-client-default:$tbdLibsVersion")
    implementation(project(":felles"))
}