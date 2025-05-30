# PHI Authentication Service

(ESP) Authentication Service provides OAuth2 Authorization Server capabilities for secure access management.

## Overview

This project implements a Spring Boot-based OAuth2 Authorization Server that supports:
- OAuth2 authentication flows
- JWT token generation and validation
- Multiple database connections (MBS and Accurint)
- Custom realm-based authentication

## Technologies

- Java 21
- Spring Boot 3.4.4
- Spring Security OAuth2 Authorization Server
- MySQL Database
- Maven
- JaCoCo (for code coverage reports)

## Prerequisites

- JDK 21+
- Maven 3.6+
- MySQL database
- Environment variables configuration (see below)

## Environment Configuration

The application requires the following environment variables to be set:

### Database Configuration
- `ESP_PHI_AUTH_MBS_DB_URL`: URL for the MBS database
- `ESP_PHI_AUTH_MBS_DB_USERNAME`: Username for the MBS database
- `ESP_PHI_AUTH_MBS_DB_PASSWORD`: Password for the MBS database
- `ESP_PHI_AUTH_ACCURINT_DB_URL`: URL for the Accurint database
- `ESP_PHI_AUTH_ACCURINT_DB_USERNAME`: Username for the Accurint database
- `ESP_PHI_AUTH_ACCURINT_DB_PASSWORD`: Password for the Accurint database

### OAuth2/OIDC Configuration
- `ESP_PHI_AUTH_OIDC_ISSUER_BASE_URL`: Base URL for the OIDC issuer
- `ESP_PHI_AUTH_AUTHORIZATION_ENDPOINT`: Authorization endpoint path
- `ESP_PHI_AUTH_TOKEN_ENDPOINT`: Token endpoint path
- `ESP_PHI_AUTH_JWKS_ENDPOINT`: JWKS endpoint path
- `ESP_PHI_AUTH_INTROSPECTION_ENDPOINT`: Token introspection endpoint path
- `ESP_PHI_AUTH_TOKEN_REVOCATION_ENDPOINT`: Token revocation endpoint path

### Client Credentials
- `ESP_PHI_AUTH_CLIENT_USERNAME`: OAuth2 client username
- `ESP_PHI_AUTH_CLIENT_SECRET`: OAuth2 client secret

Environment variables can be set in the `env/phi-auth.env` file for development.

## Build and Run

### Using Maven

```bash
# Build the application
./mvnw clean package

# Run the application
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

### Using JAR file

```bash
java -jar target/esp-project-0.0.1-SNAPSHOT.jar
```

## Testing

Run the tests using Maven:

```bash
./mvnw test
```

### Code Coverage

JaCoCo is configured to generate code coverage reports. After running tests, the reports can be found at:

```
target/site/jacoco/index.html
```

## API Endpoints

The service runs on port 8081 by default and provides the following OAuth2 endpoints:

- **Token**: `/oauth2/token`
- **JWKS**: `/oauth2/jwks`
- **Introspection**: `/oauth2/introspect`
- **Token Revocation**: `/oauth2/revoke`
