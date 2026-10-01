plugins {
    id("no.nav.sykepenger.deployable")
}

sykepengerDeployable {
    mainClass = "no.nav.helse.sparkel.institusjonsopphold.AppKt"
    imageName = "helse-sparkelapper-institusjonsopphold"
}

dependencies {
    implementation(project(":felles"))
    implementation(libs.tbd.libs.azure)

    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.wiremock) { exclude(group = "junit") }
}
