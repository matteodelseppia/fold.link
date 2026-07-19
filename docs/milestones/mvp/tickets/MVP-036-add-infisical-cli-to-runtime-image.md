# MVP-036: Add the Infisical CLI to the runtime image

## Description
Install a pinned, checksum-verified Infisical CLI binary in the container runtime stage.

## Acceptance Criteria
- CLI version matches the repository tool pin.
- Download checksum or signature is verified during build.
- No Infisical credential is embedded in an image layer.
- Testing: run `infisical --version` in the built image and inspect image history for credential values.
