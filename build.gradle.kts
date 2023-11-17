import org.flywaydb.gradle.task.FlywayMigrateTask

plugins {
    java
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    id("org.flywaydb.flyway") version "9.22.3"
}

val testContainersVersion = "1.19.1"

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
    implementation("com.rometools:rome:2.1.0")
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.testcontainers:postgresql:$testContainersVersion")
    implementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    implementation("org.junit.jupiter:junit-jupiter:5.10.0")
    implementation("org.assertj:assertj-core:3.24.2")
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
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
