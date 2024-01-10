val cxfVersion: String by project
val tjenestespesifikasjonerVersion = "1.2019.09.25-00.21-49b69f0625e0"

dependencies {
    implementation("com.sun.xml.ws:jaxws-ri:4.0.2")
    implementation("javax.xml.ws:jaxws-api:2.3.1")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion") {
        exclude("commons-collections", "commons-collections")
    }
    implementation("commons-collections:commons-collections:3.2.2")
    implementation("javax.activation:activation:1.1.1")

    implementation("no.nav.tjenestespesifikasjoner:ytelseskontrakt-v3-tjenestespesifikasjon:$tjenestespesifikasjonerVersion")
    implementation("no.nav.tjenestespesifikasjoner:nav-meldekortUtbetalingsgrunnlag-v1-tjenestespesifikasjon:$tjenestespesifikasjonerVersion")
    implementation(project(":felles"))
}
