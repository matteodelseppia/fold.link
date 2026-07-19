# fold.link - Local Toolchain

This document lists the exact tool versions supported for local development
and how to install them. Versions are pinned so every contributor (and CI)
builds and tests against the same toolchain.

## Version manager

We use [mise](https://mise.jdx.dev/) (asdf-compatible) as the version
manager. Pinned versions live in [`.mise.toml`](../../../.mise.toml) at the
repository root.

### Install mise

```bash
curl https://mise.run | sh
```

See https://mise.jdx.dev/getting-started.html for platform-specific options
(Homebrew, apt, etc.).

### Resolve the pinned toolchain

```bash
mise install
```

This reads `.mise.toml` and installs/activates the pinned Java and Node.js
versions. Expected output resolves each declared tool (e.g.
`mise java@temurin-25 ✓ installed`, `mise node@22.14.0 ✓ installed`). If a
tool cannot be installed automatically, `mise` prints the exact plugin/
installation command that failed so it can be run directly.

## Pinned versions

| Tool          | Version         | Managed by                        |
|---------------|-----------------|------------------------------------|
| Java          | 25 (Temurin)    | mise (`.mise.toml`)                |
| Node.js       | 22.14.0         | mise (`.mise.toml`)                |
| Gradle        | owned by wrapper (MVP-011) | Gradle wrapper (`gradlew`) |
| k6            | v0.54.0         | manual install (not mise-managed)  |
| Infisical CLI | v0.41.89        | manual install (not mise-managed)  |

### Java

Pinned to `temurin-25` (Eclipse Temurin distribution of JDK 25), matching
the Spring Boot 4 / Java 25 backend. Installed via `mise install`.

### Node.js

Pinned to `22.14.0` (an explicit current LTS release), used for the static
frontend tooling and the Node.js system-test runner. Installed via
`mise install`.

### Gradle

Not pinned here by design. The Gradle wrapper (added in MVP-011) owns the
Gradle version via `gradle/wrapper/gradle-wrapper.properties`, and
`./gradlew` always resolves the correct version automatically.

### k6

k6 does not have a reliable core mise/asdf plugin, so it is installed
directly. Pinned version: **v0.54.0**.

```bash
# macOS
brew install k6@0.54.0 || brew install k6   # then verify: k6 version

# Linux (Debian/Ubuntu) - install the exact pinned release
curl -L https://github.com/grafana/k6/releases/download/v0.54.0/k6-v0.54.0-linux-amd64.tar.gz \
  | tar xz --strip-components 1 -C /usr/local/bin k6-v0.54.0-linux-amd64/k6
```

Verify with `k6 version` and confirm it reports `v0.54.0`.

### Infisical CLI

Also not mise/asdf-managed by default, so it is installed directly. Pinned
version: **v0.41.89**.

```bash
# macOS
brew install infisical/get-cli/infisical

# Linux (Debian/Ubuntu) - install the exact pinned release
curl -1sLf 'https://artifacts-cli.infisical.com/setup.deb.sh' | sudo -E bash
sudo apt-get install -y infisical=0.41.89
```

Verify with `infisical --version` and confirm it reports `0.41.89`.

## Testing evidence

`mise` and `asdf` were not available in the sandbox used to author this
ticket, so `mise install` could not be executed directly here. As a
substitute, `.mise.toml` was validated for correct TOML syntax:

```bash
python3 -c "import tomllib; tomllib.load(open('.mise.toml','rb')); print('OK: .mise.toml is valid TOML')"
# -> OK: .mise.toml is valid TOML
```

A developer with `mise` installed should run:

```bash
mise install
```

Expected result: `mise` resolves `java` (Temurin 25) and `node` (22.14.0)
from `.mise.toml`, downloading and activating them if not already present,
and exits 0. If a tool/plugin cannot be resolved automatically, `mise`
prints the specific installation command needed instead of failing
silently.
