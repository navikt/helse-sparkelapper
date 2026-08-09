plugins {
    id("no.nav.helse.sas.sas-deployable")
}

sasDeployable {
    mainClass = "no.nav.helse.sparkel.medlemskap.AppKt"
    imageName = "helse-sparkelapper-medlemskap"
}

dependencies {
    implementation(project(":felles"))
    implementation(libs.tbd.libs.azure)
    implementation(libs.tbd.libs.retry)

    testImplementation(libs.rapids.and.rivers.test)
}
