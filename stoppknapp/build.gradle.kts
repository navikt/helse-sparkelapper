plugins {
    id("no.nav.helse.sas.sas-deployable")
}

sasDeployable {
    mainClass = "no.nav.helse.sparkel.stoppknapp.AppKt"
    imageName = "helse-sparkelapper-stoppknapp"
}

dependencies {
    implementation(project(":felles"))
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.jackson3)

    testImplementation(libs.rapids.and.rivers.test)
}
