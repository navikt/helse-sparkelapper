plugins {
    id("no.nav.helse.sas.sas-deployable")
}

sasDeployable {
    mainClass = "no.nav.helse.sparkel.dokumenter.AppKt"
    imageName = "helse-sparkelapper-dokumenter"
}

dependencies {
    implementation(project(":felles"))
    implementation(libs.tbd.libs.azure)
    implementation(libs.ktor.client.apache)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.jackson3)

    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.mockk)
    testImplementation(libs.wiremock) { exclude(group = "junit") }
}
