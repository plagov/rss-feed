plugins {
    java
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.flywaydb.flyway") version "11.0.1"
    id("com.github.ben-manes.versions") version "0.51.0"
}

val testContainersVersion = "1.20.4"

group = "io.plagov"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-testcontainers")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-oauth2-client")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("com.microsoft.playwright:playwright:1.49.0")
    implementation("com.rometools:rome:2.1.0")
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb.flyway-test-extensions:flyway-spring-test:10.0.0")
    implementation("org.flywaydb:flyway-database-postgresql:11.0.1")
    implementation("org.testcontainers:postgresql:$testContainersVersion")
    implementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    implementation("org.junit.jupiter:junit-jupiter:5.11.3")
    implementation("com.github.java-json-tools:json-patch:1.13")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.register<JavaExec>("playwrightInstall") {
    classpath(sourceSets["test"].runtimeClasspath)
    mainClass.set("com.microsoft.playwright.CLI")
    args = listOf("install-deps")
}
