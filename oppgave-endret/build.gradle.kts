plugins {
    id("no.nav.helse.sas.sas-deployable")
}

sasDeployable {
    mainClass = "no.nav.helse.sparkel.oppgaveendret.AppKt"
    imageName = "helse-sparkelapper-oppgave-endret"
}

dependencies {
    implementation(project(":felles"))

    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.mockk)
}
