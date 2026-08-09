plugins {
    id("no.nav.helse.sas.sas-deployable")
}

sasDeployable {
    mainClass = "no.nav.helse.sparkel.sykepengeperioderapi.AppKt"
    imageName = "helse-sparkelapper-sykepengeperioder-api"
}

dependencies {
    implementation(project(":infotrygd"))
    implementation(libs.hikaricp)
    implementation(libs.logback.classic)
    implementation(libs.logback.logstash.encoder)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt) { exclude(group = "junit") }
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.serialization.jackson3)

    testImplementation(libs.testcontainers.oracle.free)
    testImplementation(libs.mock.oauth2.server)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.jsonassert)
}
