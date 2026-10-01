plugins {
    id("no.nav.sykepenger.deployable")
}

sykepengerDeployable {
    mainClass = "no.nav.helse.sparkel.medlemskapmock.AppKt"
    imageName = "helse-sparkelapper-medlemskap-mock"
}

dependencies {
    implementation(project(":felles"))
}
