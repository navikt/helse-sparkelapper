plugins {
    id("no.nav.helse.sas.sas-deployable")
}

sasDeployable {
    mainClass = "no.nav.helse.sparkel.arbeidsgiver.AppKt"
    imageName = "helse-sparkelapper-arbeidsgiver"
}

dependencies {
    implementation(project(":felles"))
    implementation(libs.tbd.libs.azure)
    implementation(libs.tbd.libs.retry)
    implementation(libs.tbd.libs.spedisjon.client)

    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.mockk)
}
