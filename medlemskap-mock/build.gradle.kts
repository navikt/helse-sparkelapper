plugins {
    id("no.nav.helse.sas.sas-deployable")
}

sasDeployable {
    mainClass = "no.nav.helse.sparkel.medlemskapmock.AppKt"
    imageName = "helse-sparkelapper-medlemskap-mock"
}

dependencies {
    implementation(project(":felles"))
}
