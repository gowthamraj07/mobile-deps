# Known-Good Compatibility Matrix

Verified toolchain sets for Compose Multiplatform apps. Each **generation** is a
set of versions that were confirmed to build and ship *together*. The shared
catalog (`catalog/libs.versions.toml`) tracks **Gen C**, the current baseline.

A version catalog pins numbers; it does **not** tell you that a given cluster
was proven to work together. That's what this file is for — especially the
tight **Kotlin ↔ CMP ↔ KSP ↔ AGP** cluster, which must move in lockstep.

## Toolchain generations

| | **Gen A** | **Gen B** | **Gen C (baseline)** |
|---|---|---|---|
| Status | superseded | superseded | **current** |
| Verified by | app A | app B | apps C, D |
| kotlin | `2.1.21` | `2.2.21` | **`2.3.21`** |
| agp | `8.10.0` | `8.11.1` | **`8.13.2`** |
| compose-multiplatform | `1.7.0` | `1.8.2` | **`1.11.1`** |
| ksp | `2.1.21-2.0.1` | — | **`2.3.10`** |
| compileSdk | `35` | `36` | **`36`** |
| room | `2.7.1` | — | **`2.8.4`** |
| koin | `4.0.0` | — | **`4.2.2`** |
| coroutines | `1.10.2` | — | **`1.11.0`** |
| arrow | `2.0.1` | — | **`2.2.2.1`** |
| kotlinx-serialization | `1.8.0` | `1.8.1` | **`1.11.0`** |
| kotlinx-datetime | `0.6.0` | — | **`0.8.0`** |
| mockk | `1.14.2` | — | **`1.14.11`** |

### Gen C — pinning notes (why these exact numbers)

- **coreKtx `1.18.0` / lifecycle `2.10.0`** — `1.19` / `2.11` require AGP 9.1 +
  compileSdk 37. Held back to stay on AGP 8.13 / compileSdk 36.
- **arrow `2.2.2.1`** — `2.2.3`'s native klibs are built with Kotlin 2.4 (ABI
  2.4.0), unreadable by Kotlin 2.3.x. This is the newest release whose iOS klib
  ABI is ≤ 2.3.0.
- **cmpMaterial3 `1.9.0`** — Material3 is versioned independently of CMP since
  1.10. The wizard pairs CMP 1.11.1 with `1.11.0-alpha07`, which drags androidx
  material3 to `1.5.0-alpha17` on the Android classpath. Latest stable keeps
  Android behavior identical to the BOM.
- **cmpMaterialIcons `1.7.3`** — final MP release; frozen upstream.
- **robolectric `4.15.1`** — tops out at Android SDK 35 shadows; tests pin
  `@Config(sdk = 35)`.
- **playPublisher `3.13.0`** — last 3.x line; 4.0.0 drops AGP < 9 support.

## Candidate libraries — verified on Gen C (2026-07-25)

All Tier-2 libraries were built against Gen C (Kotlin 2.3.21 / CMP 1.11.1 / AGP
8.13.2) in an isolated template worktree: multiplatform libs added to
commonMain/iosMain, Android-only to androidMain, Kotest split across commonTest
(engine/assertions/property) + androidUnitTest (runner-junit5). Checks:
`compileKotlinIosSimulatorArm64` (iOS klib ABI — the decisive test), `assembleDebug`
(Android), and the Kotest test compiles. **All green.**

| library | version | shipped on | verified on 2.3.21? |
|---|---|---|---|
| ktor | `3.1.3` | Gen A/B | ✅ 2026-07-25 (iOS + Android) |
| coil3 | `3.2.0` | Gen A/B | ✅ 2026-07-25 (see Skiko note) |
| kotest | `5.9.1` | Gen A | ✅ 2026-07-25 (klibs load; JVM runner ok) |
| gitlive-firebase | `2.1.0` | Gen A/B | ✅ 2026-07-25 (iOS + Android) |
| compottie | `2.0.0-rc04` | Gen A | ✅ 2026-07-25 (iOS) |
| filekit | `0.10.0-beta02` | Gen A | ✅ 2026-07-25 (iOS + Android) |
| bouquet | `1.0.0` | Gen A | ✅ 2026-07-25 (Android) |
| uri-kmp | `0.0.19` | Gen A | ✅ 2026-07-25 (iOS) |
| firebase (Android BOM `33.13.0`) | — | Gen A/B | ✅ 2026-07-25 (assembleDebug) |
| biometric / security-crypto / core-i18n / okhttp-logging | — | Gen A | ✅ 2026-07-25 (Android) |

**Skiko note (benign):** `coil-core:3.2.0` *declares* skiko `0.9.4`, which Gradle
resolves **up** to the CMP 1.11.1 skiko `0.144.6`. A single skiko ends up on the
classpath and iOS compile/link passes; the compose plugin's "Skiko dependencies'
versions are incompatible" line is a declared-vs-resolved warning, not a real conflict.

**Not build-tested here:** the `google-services` / `firebase-crashlytics` *Gradle
plugins* (need a real `google-services.json`; they're AGP-bound, not Kotlin-ABI
sensitive) and the `io.ktor.plugin` server plugin. The Firebase/Ktor *library*
artifacts above are verified — only the plugins are unproven.

**Byproduct — a template test-compile quirk (not a catalog issue):** compiling
`commonTest` for iOS surfaced `Name contains illegal characters: ","` — backtick test
names containing commas are illegal Kotlin/Native identifiers. CI only ever compiles
tests for JVM/Android, so this was latent. Worth fixing (rename those tests) independent
of the catalog.

## How to verify a candidate

1. In a Gen C app (or a throwaway template clone), add the dependency from
   the catalog.
2. `./gradlew :composeApp:assembleDebug` **and** a native build
   (`:composeApp:linkDebugFrameworkIosSimulatorArm64`) — the iOS klib ABI is
   where Kotlin-version mismatches actually bite.
3. If green, move the row into the generation table and drop the `[UNVERIFIED]`
   marker in `catalog/libs.versions.toml`. If not, record the failing version +
   the earliest working one here.
