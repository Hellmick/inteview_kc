apply(plugin = "org.jetbrains.kotlin.plugin.allopen")

dependencies {
    implementation("org.apache.camel.quarkus:camel-quarkus-core")
    implementation("org.apache.camel.quarkus:camel-quarkus-rest")
    implementation("org.apache.camel.quarkus:camel-quarkus-bean")
    implementation("org.apache.camel.quarkus:camel-quarkus-direct")
    implementation("org.apache.camel.quarkus:camel-quarkus-http")
    implementation("org.apache.camel.quarkus:camel-quarkus-jackson")
    implementation("org.apache.camel.quarkus:camel-quarkus-kafka")
    implementation("com.nimbusds:nimbus-jose-jwt:9.37.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.2")

    testImplementation("org.apache.camel:camel-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
}

allOpen {
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
    annotation("org.apache.camel.builder.RouteBuilder")
}