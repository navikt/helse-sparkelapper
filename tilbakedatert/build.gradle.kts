plugins {
    id("no.nav.helse.sas.sas-deployable")
}

sasDeployable {
    mainClass = "no.nav.helse.sparkel.tilbakedatert.AppKt"
    imageName = "helse-sparkelapper-tilbakedatert"
}

dependencies {
    implementation(project(":felles"))
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.jackson)

    testImplementation(libs.rapids.and.rivers.test)
}
