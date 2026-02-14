plugins {
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("jacoco")
}

group = "com.nazarethlabs"
version = "x.x.x"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.github.spullara.mustache.java:compiler:0.9.10")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.12"
}

// Relatório de cobertura
tasks.jacocoTestReport.configure {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(
            sourceSets.main.get().output.asFileTree.matching {
                exclude(
                    "**/core/client/**",
                    "**/JosephApplication*",
                    "**/*Entity*",
                    "**/*Dto*",
                    "**/*Request*",
                    "**/*Response*",
                )
            },
        ),
    )
    sourceDirectories.setFrom(
        files(
            sourceSets.main
                .get()
                .allSource.srcDirs,
        ),
    )
    executionData.setFrom(files(layout.buildDirectory.file("jacoco/test.exec")))
}

// o plugin usa o arquivo .editorconfig para as regras de formatação
ktlint {
    version.set("1.3.1")
    verbose.set(true)
    outputToConsole.set(true)
}

// a verificação do Ktlint é executada junto com a task 'check' (ex: ./gradlew build)
tasks.check {
    dependsOn(tasks.ktlintCheck)
}
