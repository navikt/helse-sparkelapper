val ktorVersion: String by project
val hikariCPVersion: String by project
val jsonAssertVersion: String by project
val logbackClassicVersion = "1.5.25"
val logbackEncoderVersion = "8.0"
val jacksonVersion = "2.18.3"
val testcontainersVersion = "2.0.5"

dependencies {
    implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackEncoderVersion")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion") {
        exclude(group = "junit")
    }
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-server-call-id:$ktorVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    implementation(project(":infotrygd"))

    testImplementation("org.testcontainers:testcontainers-oracle-free:$testcontainersVersion")
    testImplementation("no.nav.security:mock-oauth2-server:2.1.10")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.skyscreamer:jsonassert:$jsonAssertVersion")
}
