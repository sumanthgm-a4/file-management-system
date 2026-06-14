# Spring Boot Template

> **IMPORTANT:** This repository is a GitHub Template Repository. When creating a new project, use **"Use this template"** and create a new repository from it.
>
> After creating a project from this template, **replace this README.md with project-specific documentation.** and **change the project name in settings.gradle**.

---

## Overview

This repository provides a pre-configured Spring Boot project template with commonly used dependencies, Docker support, GitHub Actions integration, and a Gradle Version Catalog.

The goal is to minimize setup time and provide a consistent starting point for new services and applications.

### Technology Versions

* Java 21
* Spring Boot 3.5.14
* Gradle
* Docker
* GitHub Container Registry (GHCR)

---

## Creating a New Project

1. Click **Use this template** on GitHub.
2. Create a new repository.
3. Clone the new repository.
4. Update:

   * Project name
   * Package structure
   * Application name
   * README.md
5. Uncomment any dependencies required for your project.
6. Start building.

---

## Dependency Management

Most commonly used Spring and infrastructure dependencies are already configured in:

```text
gradle/libs.versions.toml
```

and referenced in:

```groovy
build.gradle
```

Most dependencies are intentionally commented out in the `dependencies` block.

To enable a dependency, simply uncomment it.

Example:

```groovy
implementation libs.spring.kafka
```

or

```groovy
implementation libs.spring.jpa
runtimeOnly libs.postgres
```

No additional dependency declarations should be necessary for most projects.

---

## Included Dependency Catalog

> **NOTE**: **Spring Web**, **Validation** and **Lombok** starters are already enabled by default.

The version catalog already contains aliases for common dependencies including:

### Spring Boot

* Spring Web
* Spring WebFlux
* Spring Validation
* Spring Security
* Spring Actuator
* Spring Data JPA
* Spring Data MongoDB
* Spring Data Cassandra
* Spring Data Redis
* Spring Cache
* Spring Batch
* Spring Mail
* Spring AOP
* Spring WebSocket
* Spring GraphQL
* Spring RSocket
* OAuth2 Client
* OAuth2 Resource Server

### Messaging

* Spring Kafka
* Spring Cloud Stream
* Spring Cloud Stream Kafka Binder

### Cloud

* OpenFeign

### Databases

* PostgreSQL
* MySQL
* MariaDB

### Utilities

* Lombok
* MapStruct
* JWT (jjwt)
* SpringDoc OpenAPI / Swagger

---

## Docker

A Dockerfile is already included.

Build locally:

```bash
./gradlew bootJar
docker build -t my-service .
```

Run:

```bash
docker run -p 8080:8080 my-service
```

---

## GitHub Actions and Container Images

A GitHub Actions workflow is configured for this repository.

Whenever code is pushed to the configured branch:

1. The project is built.
2. A Docker image is created using the provided Dockerfile.
3. The image is pushed to GitHub Container Registry (GHCR).

Image naming convention:

```text
ghcr.io/sumanthgm-a4/<repo-name>:<commit-id>
```

Example:

```text
ghcr.io/sumanthgm-a4/payment-service:6c67c2e5e3d1b0f4c5f1b4f6e1f2d3a4b5c6d7e8
```

Pull image:

```bash
docker pull ghcr.io/sumanthgm-a4/<repo-name>:<commit-id>
```

Run image:

```bash
docker run -p 8080:8080 ghcr.io/sumanthgm-a4/<repo-name>:<commit-id>
```

---

## Upgrading Java

Java version is configured in:

```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

To upgrade:

```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
```

Also update the Docker image used in the Dockerfile.

Example:

```dockerfile
FROM eclipse-temurin:25-jre
```

And update the Java version in GHA:

Example:
```yaml
java-version: 25
```

---

## Upgrading Spring Boot

Spring Boot version is configured in:

```groovy
plugins {
    id 'org.springframework.boot' version '3.5.14'
}
```

To upgrade:

```groovy
plugins {
    id 'org.springframework.boot' version '4.0.0'
}
```

Spring Boot manages the versions of most Spring ecosystem dependencies automatically.

This includes:

* Spring Framework
* Spring Data
* Spring Security
* Spring Kafka
* Jackson
* Reactor
* Netty
* Database drivers
* Redis drivers
* MongoDB drivers
* Cassandra drivers

In most cases, upgrading Spring Boot upgrades these dependencies automatically.

---

## Upgrading Spring Cloud

Spring Cloud version is configured in:

```groovy
ext {
    set('springCloudVersion', '2025.0.0')
}
```

To upgrade:

```groovy
ext {
    set('springCloudVersion', '2026.0.0')
}
```

This affects:

* OpenFeign
* Spring Cloud Stream
* Spring Cloud Stream Kafka Binder

---

## Upgrading Non-Spring Managed Dependencies

Some libraries are not managed by Spring Boot and must be upgraded manually.

These are defined in:

```text
gradle/libs.versions.toml
```

Current manually managed dependencies include:

* Lombok
* MapStruct
* SpringDoc OpenAPI
* JJWT

Example:

```toml
[versions]

lombok = "1.18.42"
mapstruct = "1.6.3"
springdoc = "2.8.9"
jjwt = "0.12.7"
```

Update these versions directly when required.

---

## Building

Build:

```bash
./gradlew build
```

Run tests:

```bash
./gradlew test
```

Create executable jar:

```bash
./gradlew bootJar
```

---

## Recommended Steps After Creating a Project

* Rename packages
* Update application name
* Update group ID
* Replace README.md
* Remove unused dependencies
* Configure application properties
* Configure environments
* Configure database settings
* Configure CI/CD as needed
* Make the first commit

---

## Notes

* This repository is intended to be a starting point, not a finished application.
* Only enable dependencies that are actually needed.
* Prefer upgrading Spring Boot before manually upgrading Spring ecosystem libraries.
* Keep Java, Spring Boot, Docker image versions, and CI configuration aligned.
