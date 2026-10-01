plugins {
    id("no.nav.sykepenger.deployable")
}

sykepengerDeployable {
    mainClass = "no.nav.helse.sparkel.representasjon.AppKt"
    imageName = "helse-sparkelapper-representasjon"
}

dependencies {
    implementation(project(":felles"))
    implementation(libs.tbd.libs.azure)
    implementation(libs.ktor.client.apache)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.jackson3)

    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.mockk)
    testImplementation(libs.wiremock) { exclude(group = "junit") }
}
