# MVP-096: Create the system-test harness

## Description
Create a Node.js black-box test harness that starts the packaged application with disposable Redis or targets an explicitly supplied base URL.

## Acceptance Criteria
- Local mode allocates collision-safe ports and cleans up child processes.
- Remote mode cannot mutate production unless an explicit safety flag is present.
- Readiness waits are bounded and failure logs are retained.
- Testing: run both local mode and a fixture remote mode; force startup failure and verify cleanup.
