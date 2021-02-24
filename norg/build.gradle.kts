val ktorVersion: String by project
val cxfVersion: String by project

dependencies {
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-logging-jvm:$ktorVersion")

    implementation("no.nav.tjenestespesifikasjoner:person-v3-tjenestespesifikasjon:1.2019.09.25-00.21-49b69f0625e0")

    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")

    implementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
    implementation("org.glassfish.jaxb:jaxb-runtime:2.4.0-b180830.0438")
    implementation("com.sun.activation:javax.activation:1.2.0")
    implementation("com.sun.xml.ws:jaxws-tools:2.3.3") {
        exclude(group = "com.sun.xml.ws", module = "policy")
    }
}
