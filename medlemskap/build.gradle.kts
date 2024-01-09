val tbdLibsVersion: String by project
dependencies {
    implementation("com.github.navikt.tbd-libs:azure-token-client:$tbdLibsVersion")
    implementation(project(":felles"))
}