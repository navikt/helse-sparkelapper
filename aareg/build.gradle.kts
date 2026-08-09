plugins {
    id("no.nav.helse.sas.sas-deployable")
}

sasDeployable {
    mainClass = "no.nav.helse.sparkel.aareg.AppKt"
    imageName = "helse-sparkelapper-aareg"
}

dependencies {
    implementation(project(":felles"))
    implementation(libs.tbd.libs.azure)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.apache)
    implementation(libs.ktor.client.jackson)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.jackson3)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)

    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.mock.oauth2.server)
    testImplementation(libs.mockk)
    testImplementation(libs.wiremock) { exclude(group = "junit") }
    testImplementation(libs.ktor.client.mock)
}
