# Copilot Instructions - ms-checkout

## Project Overview
This is a Spring Boot 3.5.7 microservice for checkout operations, part of an event-driven architecture (ArquiteturaOrientadaEventos). The service is in early development stage with minimal implementation.

## Tech Stack
- **Java 21** with Spring Boot 3.5.7
- **Build Tool**: Gradle 8.x with Gradle Wrapper
- **Package Structure**: `io.resousadev.linuxtips.ms_checkout` (note: underscore, not hyphen)
- **Testing**: JUnit 5 Platform with Spring Boot Test

## Project Structure
```
src/main/java/io/resousadev/linuxtips/ms_checkout/
  └── MsCheckoutApplication.java (main entry point)
src/main/resources/
  └── application.yaml (Spring configuration)
```

## Development Workflows

### Building the Project
```powershell
# Windows PowerShell
.\gradlew.bat build

# Or use the alias
.\gradlew build
```

### Running the Application
```powershell
.\gradlew.bat bootRun
```

### Running Tests
```powershell
.\gradlew.bat test
```

### Clean Build
```powershell
.\gradlew.bat clean build
```

## Git Configuration
- **Repository**: `git@gh-resousadev:resousadev/ArquiteturaOrientadaEventos.git`
- **SSH Host Alias**: Uses custom SSH config `gh-resousadev`
- **Main Branch**: `main`
- **User**: resousadev@gmail.com

## Package Naming Convention
⚠️ **Important**: The original package name `ms-checkout` (with hyphen) is invalid in Java. The project uses `ms_checkout` (with underscore) instead. When creating new classes, always use the underscore convention:
```java
package io.resousadev.linuxtips.ms_checkout;
```

## Current Architecture
This is a minimal Spring Boot starter with:
- Basic web starter dependency
- Single application entry point
- No controllers, services, or repositories yet
- Default Spring Boot configuration

## AWS Event-Driven Architecture
This microservice uses AWS native services for event-driven communication:
- **Amazon EventBridge**: Event bus for publishing and routing domain events
- **Amazon SQS**: Message queuing for asynchronous processing
- **AWS SDK for Java 2.x**: Required for AWS service integration

## Next Development Steps
When extending this microservice, follow these patterns:
- Add AWS SDK dependencies (EventBridge, SQS)
- Implement EventBridge event publishers for checkout domain events
- Configure SQS consumers for async message processing
- Add REST endpoints for checkout operations
- Set up AWS credentials and region configuration in `application.yaml`
