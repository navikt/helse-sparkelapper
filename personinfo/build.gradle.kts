plugins {
    id("no.nav.helse.sas.sas-deployable")
}

sasDeployable {
    mainClass = "no.nav.helse.sparkel.personinfo.AppKt"
    imageName = "helse-sparkelapper-personinfo"
}

dependencies {
    implementation(project(":felles"))
    implementation(libs.tbd.libs.azure)
    implementation(libs.tbd.libs.speed.client)
    implementation(libs.tbd.libs.retry)
    implementation(libs.avro)
    implementation(libs.ktor.client.apache)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.jackson3)

    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.jsonassert)
    testImplementation(libs.mockk)
}
