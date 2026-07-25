# mobile-deps

Shared dependency version catalog + compatibility matrix for my Compose
Multiplatform apps. One source of truth for **which library versions are known
to work together**.

- **[`catalog/libs.versions.toml`](catalog/libs.versions.toml)** — the catalog.
- **[`KNOWN-GOOD.md`](KNOWN-GOOD.md)** — verified toolchain generations + the
  tight Kotlin/CMP/KSP/AGP cluster, with the *reasons* behind each pin.

Two ways to consume it, depending on whether you want build-enforced or just a
reference.

## 1. Gradle import (build-enforced, for my own apps)

Publish the catalog, then import it in each app's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/gowthamraj07/mobile-deps")
            // GitHub Packages requires auth to READ, even for public repos.
            // Put these in ~/.gradle/gradle.properties (never commit them):
            //   gpr.user=gowthamraj07
            //   gpr.key=<a PAT with read:packages scope>
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    versionCatalogs {
        create("libs") {
            from("io.github.gowthamraj07:cmp-catalog:2026.07.0")
            // per-app overrides are still allowed after `from(...)`:
            // version("kotlin", "2.3.30")
        }
    }
}
```

Usage in build scripts is unchanged — `libs.koin.core`, `libs.plugins.ksp`, etc.

> **Note:** because GitHub Packages gates reads behind a token, the "everyone
> else" audience below can't `from(...)` this without a PAT. That's the
> reference-only path (§2) — or switch to Maven Central later for no-auth access.

## 2. Reference only (for everyone else)

No build coupling — copy the versions you need, or vendor the file:

```kotlin
versionCatalogs {
    create("libs") {
        from(files("gradle/cmp-catalog.versions.toml")) // a copy of this catalog
    }
}
```

…or just read `KNOWN-GOOD.md` to see what pairs with what.

## Publishing (maintainer)

```bash
# GitHub Packages (default; consumers need a GitHub token to read)
GITHUB_ACTOR=gowthamraj07 GITHUB_TOKEN=*** ./gradlew publish
```

For **truly public, no-auth** consumption, publish to Maven Central instead
(swap in the `com.vanniktech.maven.publish` plugin in `build.gradle.kts`).
JitPack does not serve `version-catalog` artifacts cleanly, so Central is the
path for "anyone can `from(...)`".

## Versioning

Calendar versioned `YYYY.MM.PATCH`. Bump `PATCH` for same-month revisions. Each
release should correspond to a verified generation in `KNOWN-GOOD.md`.

## Tiers

The catalog is split into two tiers:

- **Verified** — build-proven on the current baseline (Kotlin 2.3.21 / CMP
  1.11.1 / AGP 8.13.2) by shipping apps.
- **`[UNVERIFIED on Kotlin 2.3.21]`** — libraries only used by apps still on
  older Kotlin. Pinned at their last-shipped versions; build once on the
  baseline before trusting. See `KNOWN-GOOD.md` for the verification steps.
