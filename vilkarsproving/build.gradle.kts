val ktorVersion: String by project
val cxfVersion = "3.4.0"

dependencies {
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")

    implementation("com.sun.xml.ws:jaxws-ri:2.3.3")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation("javax.activation:activation:1.1.1")

    implementation("no.nav.tjenestespesifikasjoner:egenansatt-v1-tjenestespesifikasjon:1.2019.09.25-00.21-49b69f0625e0")

    testImplementation("no.nav:kafka-embedded-env:2.4.0")
    testImplementation("org.awaitility:awaitility:4.0.3")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion")
}