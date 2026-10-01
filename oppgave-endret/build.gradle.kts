plugins {
    id("no.nav.sykepenger.deployable")
}

sykepengerDeployable {
    mainClass = "no.nav.helse.sparkel.oppgaveendret.AppKt"
    imageName = "helse-sparkelapper-oppgave-endret"
}

dependencies {
    implementation(project(":felles"))

    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.mockk)
}
