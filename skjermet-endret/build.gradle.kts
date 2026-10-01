plugins {
    id("no.nav.sykepenger.deployable")
}

sykepengerDeployable {
    mainClass = "no.nav.helse.sparkel.skjermetendret.AppKt"
    imageName = "helse-sparkelapper-skjermet-endret"
}

dependencies {
    implementation(project(":felles"))

    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.mockk)
}
