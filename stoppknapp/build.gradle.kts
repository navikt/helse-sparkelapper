plugins {
    id("no.nav.sykepenger.deployable")
}

sykepengerDeployable {
    mainClass = "no.nav.helse.sparkel.stoppknapp.AppKt"
    imageName = "helse-sparkelapper-stoppknapp"
}

dependencies {
    implementation(project(":felles"))
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.jackson3)

    testImplementation(libs.rapids.and.rivers.test)
}
