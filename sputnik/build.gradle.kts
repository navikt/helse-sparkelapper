plugins {
    id("no.nav.sykepenger.deployable")
}

sykepengerDeployable {
    mainClass = "no.nav.helse.sparkel.sputnik.AppKt"
    imageName = "helse-sparkelapper-sputnik"
}

dependencies {
    implementation(project(":felles"))
    implementation(libs.tbd.libs.azure)
    implementation(libs.tbd.libs.retry)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.jackson3)

    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.jsonassert)
    testImplementation(libs.wiremock) { exclude(group = "junit") }
}
