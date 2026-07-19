# fold.link - MVP - Requirements

## 1. Functional Requirements

- **F01**: The system shall accept a valid long URL from the user and generate a unique shortened URL alias.
- **F02**: The system shall redirect users to the original long URL when a valid shortened URL is accessed.
- **F03**: The system shall return a "404 Not Found" if a user attempts to access a shortened URL that does not exist.
- **F04**: The system shall provide a web-based user interface to allow users to submit long URLs and copy the resulting short URLs.
- **F05**: The system shall reject malformed URLs with a clear validation error.

## 2. Non-Functional Requirements

- **NF01**: The system shall persist URL mappings so that shortened URLs remain functional across system restarts and deployments.
- **NF02**: The system shall securely manage all environment configurations and secrets required for operation.
- **NF03**: The system shall be designed and tested for a read-heavy workload, where redirections substantially outnumber URL-creation requests.
- **NF04**: A generated alias shall be unique and unguessable enough to make accidental collisions practically negligible.
