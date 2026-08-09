plugins {
    id("no.nav.helse.sas.sas-deployable")
}

sasDeployable {
    mainClass = "no.nav.helse.sparkel.sykepengeperiodermock.AppKt"
    imageName = "helse-sparkelapper-sykepengeperioder-mock"
}

dependencies {
    implementation(project(":felles"))
    implementation(libs.tbd.libs.naisful.app)
    implementation(libs.rapids.and.rivers.impl)
    implementation(libs.ktor.serialization.jackson3)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.module.kotlin)

    testImplementation(libs.rapids.and.rivers.test)
}
