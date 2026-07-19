# MVP-028: Add the container health check

## Description
Configure the container to report application liveness using the actuator endpoint and a tool available in the runtime image.

## Acceptance Criteria
- Interval, timeout, retries, and start period are explicit.
- The probe uses the runtime port rather than a hard-coded development-only port.
- A hung or stopped application becomes unhealthy.
- Testing: run the image, observe healthy status, stop the Java process, and observe unhealthy status.
