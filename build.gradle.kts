import org.flywaydb.gradle.task.FlywayMigrateTask

plugins {
    java
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    id("org.flywaydb.flyway") version "10.15.0"
    id("com.github.ben-manes.versions") version "0.51.0"
}

val testContainersVersion = "1.19.8"

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
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("com.microsoft.playwright:playwright:1.44.0")
    implementation("com.rometools:rome:2.1.0")
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb.flyway-test-extensions:flyway-spring-test:10.0.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.15.0")
    implementation("org.testcontainers:postgresql:$testContainersVersion")
    implementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    implementation("org.junit.jupiter:junit-jupiter:5.10.2")
    implementation("com.github.java-json-tools:json-patch:1.13")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

task("flywayMigrateLocal", FlywayMigrateTask::class) {
    description = "Migrates the development database"
    url = "jdbc:postgresql://localhost:5432/test"
    user = "test"
    password = "test"
    locations = arrayOf(
        "filesystem:src/main/resources/db/migration",
        "filesystem:src/test/resources/flyway/migrations_local"
    )
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
