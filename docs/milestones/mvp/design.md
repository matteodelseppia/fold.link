# fold.link - Architecture and Test Plan

## 1. Architectural Overview

### 1.1 Components and Technologies
```mermaid
flowchart LR
    User[User Browser]
    FE[Static Frontend<br/>HTML / CSS / JS]
    API[Spring Boot 4 Backend<br/>Java 25]
    Redis[(Redis<br/>URL mappings)]
    
    User --> FE
    FE -->|Generate short URL| API
    User -->|Open short URL| API
    API -->|Read / write mappings| Redis
    API -->|HTTP redirect| User
```
- **Backend Service**: Developed using Spring Boot 4 and Java 25. It will expose REST APIs for URL generation and handle the HTTP redirection logic.
- **Frontend**: A static web interface (HTML/CSS/JS) served directly by the Spring Boot backend to keep the MVP simple and cohesive.
- **Persistent Storage**: Redis will be used as the primary data store. Redis provides in-memory read speeds (crucial for fast redirections) while offering persistence mechanisms (RDB/AOF) to ensure data is not lost between restarts.

### 1.2 Secrets Management
Sensitive configuration (e.g., Redis credentials, API keys) will be stored in Infisical. 

Railway deployment will fetch these secrets at runtime (e.g., using the Infisical CLI agent to inject environment variables into the container).

### 1.3 Release/Testing Strategy
```mermaid
flowchart TD
    Dev[Developer] -->|Create Pull Request| PR[GitLab Merge Request]

    PR --> PRCI[PR CI Pipeline]
    PRCI --> Unit[Unit Tests<br/>JUnit 5 + Mockito]
    PRCI --> SystemCI[System Tests<br/>Node.js Test Runner]
    PRCI --> LoadCI[Load Tests<br/>k6]

    Unit --> PRGate{All tests pass?}
    SystemCI --> PRGate
    LoadCI --> PRGate

    PRGate -->|No| Reject[Block merge]
    PRGate -->|Yes| Main[Merge to main]

    Main --> MainCI[Main CI Pipeline<br/>Rebuild application]
    MainCI --> Retest[Run unit, system and load tests<br/>in disposable CI environment]
    Retest --> MainGate{All tests pass?}

    MainGate -->|No| StopMain[Stop release]
    MainGate -->|Yes| Images[Build container images]
    Images --> Registry[Push images to<br/>GitLab Container Registry]

    Registry --> Staging[Deploy images to Railway Staging]
    Staging --> StagingTests[Run system and load tests<br/>against Staging]
    StagingTests --> StagingGate{Staging tests pass?}

    StagingGate -->|No| StopStaging[Stop promotion]
    StagingGate -->|Yes| Production[Deploy approved image<br/>to Railway Production]
```
