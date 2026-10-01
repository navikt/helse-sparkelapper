plugins {
    id("no.nav.sykepenger.deployable")
}

sykepengerDeployable {
    mainClass = "no.nav.helse.sparkel.tilbakedatert.AppKt"
    imageName = "helse-sparkelapper-tilbakedatert"
}

dependencies {
    implementation(project(":felles"))
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.jackson)

    testImplementation(libs.rapids.and.rivers.test)
}
