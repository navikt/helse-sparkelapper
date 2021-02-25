val cxfVersion = "3.3.7"
val ktorVersion: String by project

dependencies {
    implementation("io.arrow-kt:arrow-core-data:0.11.0")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")

    implementation("no.nav.tjenestespesifikasjoner:arbeidsforholdv3-tjenestespesifikasjon:1.2019.01.16-21.19-afc54bed6f85")
    implementation("no.nav.tjenestespesifikasjoner:nav-fim-organisasjon-v5-tjenestespesifikasjon:1.2019.01.16-21.19-afc54bed6f85")
    implementation("com.sun.xml.ws:jaxws-ri:2.3.3")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-policy:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("javax.activation:activation:1.1.1")

    testImplementation("io.mockk:mockk:1.10.0")
}
