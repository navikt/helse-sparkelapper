plugins {
    id("no.nav.helse.sas.sas-deployable")
}

sasDeployable {
    mainClass = "no.nav.helse.sparkel.sykepengeperioder.AppKt"
    imageName = "helse-sparkelapper-sykepengeperioder"
}

dependencies {
    implementation(project(":felles"))
    implementation(project(":infotrygd"))
    implementation(libs.hikaricp)

    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.h2)
    testImplementation(libs.flyway.database.postgresql)
}
