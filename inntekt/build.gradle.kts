plugins {
    id("no.nav.helse.sas.sas-deployable")
}

sasDeployable {
    mainClass = "no.nav.helse.sparkel.inntekt.AppKt"
    imageName = "helse-sparkelapper-inntekt"
}

dependencies {
    implementation(project(":felles"))
    implementation(libs.tbd.libs.azure)
    implementation(libs.tbd.libs.retry)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.jackson3)
    implementation(libs.jackson.module.kotlin)

    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.mockk)
    testImplementation(libs.ktor.client.mock)
}
