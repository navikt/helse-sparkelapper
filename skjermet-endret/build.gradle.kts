val avroVersion: String by project
dependencies {
    implementation("org.apache.avro:avro:$avroVersion")
    implementation(project(":felles"))
}