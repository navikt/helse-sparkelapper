val cxfVersion = "4.0.0"

dependencies {
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
    implementation("jakarta.xml.ws:jakarta.xml.ws-api:4.0.0")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.1")

    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation(project(":felles"))
}

repositories {
    // cxf versjon >= 4.0.3 er avhengig av org.opensaml:opensaml-saml-impl:4.3.0
    // som i skrivende stund ikke er tilgjengelig på maven central, men i shibboleth
    maven("https://build.shibboleth.net/maven/releases/") // så lenge vi er avhengig av cxf
}

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/main/java")
    }
}
