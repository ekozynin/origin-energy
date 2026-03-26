plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.spring") version "2.1.10"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.12.0"
    jacoco
}

group = "com.origin.exercise"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_23
    targetCompatibility = JavaVersion.VERSION_23
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.28")
    implementation("jakarta.validation:jakarta.validation-api")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_23)
    }
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/src/main/resources/openapi.yml")
    outputDir.set("${layout.buildDirectory.get()}/generated/openapi")
    apiPackage.set("com.origin.exercise.urlshortener.api")
    modelPackage.set("com.origin.exercise.urlshortener.api.model")
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useSpringBoot3" to "true",
            "documentationProvider" to "none",
            "useTags" to "true",
            "skipDefaultInterface" to "false",
            "reactive" to "false",
            "serviceInterface" to "false"
        )
    )
}

sourceSets {
    main {
        kotlin {
            srcDir("${layout.buildDirectory.get()}/generated/openapi/src/main/kotlin")
        }
    }
}

tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateApiDocs") {
    generatorName.set("html2")
    inputSpec.set("$projectDir/src/main/resources/openapi.yml")
    outputDir.set("${layout.buildDirectory.get()}/generated/api-docs")
    doLast {
        copy {
            from("${layout.buildDirectory.get()}/generated/api-docs/index.html")
            into("$projectDir")
            rename("index.html", "API-DOCS.html")
        }
    }
}

tasks.named("build") {
    dependsOn("generateApiDocs")
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude("com/origin/exercise/urlshortener/api/**")
                exclude("com/origin/exercise/urlshortener/ApplicationKt.class")
                exclude("com/origin/exercise/urlshortener/Application.class")
            }
        })
    )
}
