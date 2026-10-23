# mobile-deps — Project Status Report

**Date:** 2026-10-22 · **Group:** Fleet tooling · **Commit:** `e086a8d` (main) ·
**Last code/catalog change:** 2026-07-26 (88 days ago) · **Only release tag:** `v2026.07.25`

## Preflight caveats — read before acting on any number

1. **No knowledge graph for this repo.** `mobile-deps` is not among the 26 repos in the
   code-review-graph registry. There is also almost nothing to index: 11 tracked files,
   2 `.kts`, 1 `.toml`, 2 `.md`, 1 `.yml`. All evidence below is the filesystem and git
   history. No fallback was needed — this repo *is* its own evidence.
2. **No `openspec/`, no prior report.** This is the first run, so **there is no delta**.
   The module list was derived by hand (see Method); Behaviour was **judged against each
   module's stated promises, not counted from a spec**.
3. **No builds, publishes or dependency resolution were run** (session constraint; 16
   audits in parallel). Every statement about whether a pin *resolves* or is *current* is
   therefore a read of the files, not a resolution test. Where currency cannot be
   established offline, it is marked `unverifiable offline` rather than guessed.
4. **Working tree clean** on `main`, nothing uncommitted. But **HEAD is 2 commits ahead of
   the only tag** — see Correctness debt.
5. The GitHub MCP server failed to connect this session, so **the outcome of the one
   publish workflow run could not be confirmed** via the Actions API.

## The objective

Quoted verbatim from `README.md` (the repo has no memory file and no `openspec/config.yaml`):

> Shared dependency version catalog + compatibility matrix for my Compose
> Multiplatform apps. One source of truth for **which library versions are known
> to work together**.

And from `KNOWN-GOOD.md`:

> A version catalog pins numbers; it does **not** tell you that a given cluster
> was proven to work together. That's what this file is for — especially the
> tight **Kotlin ↔ CMP ↔ KSP ↔ AGP** cluster, which must move in lockstep.

Operationally: *every mobile app builds against one agreed set of library versions, and
that set is known to work together.* The two clauses are the two alignment axes.

### Alignment axes

| Axis | Weight | Meaning (the objective's own words) |
|---|---|---|
| **A1 — One agreed set** | 3 | "One source of truth" — the pinned set covers the fleet and nothing shadows it |
| **A2 — Known to work together** | 2 | Proven compatibility, and *kept* proven as upstream moves |
| **EN — Enablers** | 1 | The plumbing nobody asks for: publishing the artifact to GitHub Packages |

A1 carries the higher weight because it is the primary clause of the stated objective and
because a shadowed or missing pin diverges apps *silently*.

## Headline

**Overall 65%** — first run, **no delta available** (no prior report, no `scores.csv`).

| Axis | Score |
|---|---|
| **A1 — One agreed set** | **70%** |
| **A2 — Known to work together** | **60%** |
| **EN — Enablers** | **60%** |

Read it as: the catalog itself is good and genuinely adopted — four apps import it — but
the *contract around it* is where the fleet-wide risk sits. The documented import
coordinate does not exist, two apps shadow versions the catalog already pins, and nothing
anywhere notices when the set goes stale.

### Blast radius note

This repo has 11 files and four consumers: `NEPtune-Mobile`, `DriveSmart`, `AdaptiveVol`,
`cmp-template` (all pinning `cmp-catalog:2026.07.25`). It is scored on consequence, not
size. A wrong number here is wrong in four apps at once.

## Module table

Sorted by axis, then score ascending — least complete first.

| Module | Axis | Score | Label | reqs | bugs | gaps | tests / proof | surface | What's missing |
|---|---|---|---|---|---|---|---|---|---|
| **Consumption contract** | A1 | **65%** | 🟡 Partial | 5 | 1 | 1 | 1 empirical (4 apps' `settings.gradle.kts`); 0 automated | Gradle `from(...)` import | The README's import line pins a version that was never tagged; nothing documents that apps must still declare local extras |
| **Catalog coverage & coherence** | A1 | **70%** | 🟡 Partial | 6 | 2 | 2 | 10/13 plugin+lib groups build-proven (KNOWN-GOOD, dated) | `libs.*` / `libs.plugins.*` in 4 apps | 2 of 4 apps re-declare versions the catalog already pins; tier/rationale comments are stripped on publish; 3 pre-release pins |
| **Version-bump & update hygiene** | A2 | **40%** | 🟡 Partial | 6 | 2 | 2 | none | git tag | No update automation at all; a manual publish mints a phantom `2026.07.0`; the one tag violates the documented version scheme |
| **Docs & compatibility matrix** | A2 | **80%** | 🟢 Shipped, with gaps | 7 | 1 | 1 | matrix claims carry dates + the exact Gradle tasks used | `README.md`, `KNOWN-GOOD.md` | The rationale never reaches a consumer: Gradle strips all 48 comments on publish and `KNOWN-GOOD.md` is not in the artifact |
| **Publishing to GitHub Packages** | EN | **60%** | 🟡 Partial | 7 | 1 | 1 | 1 real run (tag at the CI-fix commit), unconfirmed | `publish.yml` on `v*` tags | No PR check that the catalog even builds — the tag push is the first and only gate; no proof the release landed |

### Per-module evidence

**Catalog coverage & coherence** — A1 · 70% (Behaviour 35/50 · Correctness 8/20 · Verification 15/20 · Reachability 10/10)

Promises judged: coherent Kotlin↔CMP↔KSP↔AGP cluster ✅; covers what the fleet uses ◐;
bundles + plugin aliases so apps don't re-declare ✅ (3 bundles, 13 plugins); marks which
pins are verified ◐; avoids pre-release pins in a fleet-wide catalog ✗; no dead entries
`unverifiable offline`.

- **Coherence is the strongest thing in the repo.** All 13 Gen C rows in `KNOWN-GOOD.md`
  match `catalog/libs.versions.toml` exactly — kotlin `2.3.21`, agp `8.13.2`, cmp
  `1.11.1`, ksp `2.3.10`, compileSdk `36`, room `2.8.4`, koin `4.2.2`, coroutines
  `1.11.0`, arrow `2.2.2.1`, serialization `1.11.0`, datetime `0.8.0`, mockk `1.14.11`.
  No drift between the matrix and the catalog.
- **Coverage is not complete, and the gap is the expensive kind.** 2 of 4 consumers carry
  local extras: NEPtune-Mobile 6, DriveSmart 3. Two of those extras *shadow pins the
  catalog already holds* — see Correctness debt.
- **Pre-release pins in a fleet-wide set:** `securityCrypto = "1.1.0-alpha07"`,
  `compottie = "2.0.0-rc04"`, `filekit = "0.10.0-beta02"`.
- **Currency: unverifiable offline.** The catalog's own notes reference upstream releases
  that already existed when it was written (arrow `2.2.3` built with Kotlin 2.4; coreKtx
  `1.19`/lifecycle `2.11` needing AGP 9.1 + compileSdk 37), so the baseline was already
  deliberately held back in July. Whether 88 days have since made any Tier-1 pin unsafe
  cannot be determined without network access, and this report does not guess.

**Consumption contract** — A1 · 65% (Behaviour 35/50 · Correctness 13/20 · Verification 5/20 · Reachability 10/10)

Promises judged: copy-pasteable import that works ✗; documents the consumer-authenticates-
as-themselves auth rule ✅ (clearly, and mirrored verbatim into all four apps); documents
the reference-only no-auth path ✅; documents that apps must still declare local extras ✗
(absent); per-app override after `from(...)` ✅.

- Four apps consume it and all four pin `2026.07.25`. The README pins `2026.07.0`.
- The README's only post-`from(...)` example is a `version("kotlin", ...)` override. The
  real-world need is `library(...)` additions — six of them in NEPtune-Mobile, whose
  adoption commit is literally titled *"build: adopt shared version catalog (import +
  local extras)"*. That the catalog cannot cover everything is true, reasonable, and
  completely undocumented.

**Version-bump & update hygiene** — A2 · 40% (Behaviour 25/50 · Correctness 8/20 · Verification 0/20 · Reachability 5/10)

Promises judged: `YYYY.MM.PATCH`, PATCH for same-month revisions ✗; each release matches a
verified generation in KNOWN-GOOD ◐; a documented verify-and-promote procedure ✅ (3 steps,
and it names the decisive check — the iOS klib ABI — which is exactly right); pins kept
current ✗ (no mechanism); release what's on main ◐; local publishes don't collide with CI ◐.

- The one tag `v2026.07.25` uses the **day of month as PATCH**. A second July revision
  would have to be `2026.07.26`, conflating date with patch forever.
- That tag points at `57716de`, i.e. **before** `8f026bc` recorded the Gen C Tier-2
  verification. The shipped release's matrix predates the verification its catalog
  comments now claim.
- `.github/` contains exactly one file: `publish.yml`. No Dependabot, no Renovate, no
  scheduled check. Nothing in the fleet will ever notice this catalog going stale.

**Docs & compatibility matrix** — A2 · 80% (Behaviour 46/50 · Correctness 11/20 · Verification 15/20 · Reachability 10/10)

Promises judged: states what it is and both consumption modes ✅; records the *reasons*
behind each pin ✅; generation table of sets proven to ship together ✅ (Gen A/B/C);
how-to-verify-a-candidate ✅; matrix in sync with the catalog ✅ (all 13 rows checked);
rationale reachable from the published artifact ✗; carries the defects it discovers ✅.

This is the best-executed module and deserves saying so. The pinning notes record the
actual constraint, not just the number — *arrow `2.2.3`'s native klibs are built with
Kotlin 2.4 (ABI 2.4.0), unreadable by Kotlin 2.3.x*; *cmpMaterial3 is versioned
independently of CMP since 1.10*; *robolectric tops out at SDK 35 shadows*. It also
distinguishes a benign warning (the Skiko declared-vs-resolved note) from a real conflict,
and it flags a defect it found in a *different* repo (comma-in-backtick-test-name illegal
as a Kotlin/Native identifier) while explicitly saying that is a template issue, not a
catalog one. That is the discipline this rubric is trying to reward.

**Publishing to GitHub Packages** — EN · 60% (Behaviour 36/50 · Correctness 15/20 · Verification 5/20 · Reachability 5/10)

Promises judged: publish on a version tag with no manual steps ✅; derive version from tag
✅ (`${GITHUB_REF_NAME#v}` → `-PcatalogVersion`); built-in `GITHUB_TOKEN`, no PAT ✅;
least-privilege permissions ✅ (`contents: read`, `packages: write`); stable coordinates ✅
(`io.github.gowthamraj07:cmp-catalog`); validate before publishing ✗; prove the release
landed ✗.

The workflow is small and correct, and the `GITHUB_ACTOR`-is-a-reserved-prefix pitfall is
both fixed (`57716de`) and documented inline so it won't be reintroduced. What's missing is
a gate: a malformed `libs.versions.toml` pushed with a tag goes straight to four apps.

## Correctness debt

Ranked by consequence. These jump the development-plan queue regardless of module score.

| Sev | Module | Item | Consequence |
|---|---|---|---|
| **High** | Consumption contract, Docs | `README.md` documents `from("io.github.gowthamraj07:cmp-catalog:2026.07.0")`, but the only tag ever pushed is `v2026.07.25`, and all four real consumers pin `2026.07.25`. No `2026.07.0` was ever tagged. | The repo's own copy-paste onboarding line almost certainly does not resolve. First thing a new app does, first thing that fails. (Not resolution-tested — network checks were out of scope — but no tag maps to it.) |
| **Medium** | Catalog coverage | Two consumers locally re-declare versions the catalog already pins: `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:2.10.0` in **both** NEPtune-Mobile and DriveSmart (catalog already holds `jbLifecycle = "2.10.0"`), and the `kotlin-jvm` plugin at `2.3.21` in DriveSmart (catalog already holds `kotlin = "2.3.21"`). | Shadow pins. Bump `jbLifecycle` in the catalog and those two apps silently stay on 2.10.0 — the exact divergence "one source of truth" exists to prevent. The catalog is missing a `jb-lifecycle-viewmodel` library alias and a `kotlin-jvm` plugin alias. |
| **Medium** | Catalog coverage, Docs | Gradle's `version-catalog` plugin **strips every comment** when it generates the published artifact: 48 comment lines in `catalog/libs.versions.toml`, 3 in `build/version-catalog/libs.versions.toml`. `KNOWN-GOOD.md` is not part of the artifact either. | A consuming app sees bare numbers. The three pins explicitly marked *not build-tested* — `io.ktor.plugin`, `com.google.gms.google-services`, `com.google.firebase.crashlytics` — ship with no warning attached. Every "why this exact number" note is invisible at the point of use. |
| **Medium** | Hygiene | `build.gradle.kts` falls back to a hard-coded `version = "2026.07.0"` when `-PcatalogVersion` is absent. A local build output dated 2026-10-07 sits in `build/` carrying exactly that default. | Any `./gradlew publish` run without the flag mints a version matching no tag and no verified generation — and `2026.07.0` is precisely the phantom version the README tells people to import. The two bugs reinforce each other. |
| **Medium** | Publishing | No PR/validation workflow. `publish.yml` is the only file in `.github/workflows/`, and it fires on the tag. | Nothing ever checks that the catalog parses or builds before it reaches four apps. The failure mode is fleet-wide and arrives at release time. |
| Low | Hygiene | `v2026.07.25` violates the documented `YYYY.MM.PATCH` scheme by using the day as PATCH. | Next same-month revision has no sane number. |
| Low | Catalog | 3 pre-release pins (`securityCrypto` alpha, `compottie` rc, `filekit` beta) in a set four apps share. | Pre-release artifacts can be re-published or yanked upstream; a shared catalog is the worst place to carry them. |
| Low | Hygiene | HEAD is 2 commits ahead of the only tag (`8f026bc`, `e086a8d`). | Benign in effect — both are comment/doc-only and comments are stripped on publish, so the published *versions* are identical to HEAD. Worth re-tagging anyway so the repo and the artifact agree. |

## Unspecified surface

- **`[libraries]` instrumented-test block** — 15 androidx-test/espresso/orchestrator
  entries. The fleet baseline (per the `cmp-bootstrap` conventions and every consumer's
  own setup) tests with Roborazzi + Robolectric + Kotest, not instrumented Espresso. These
  entries may be dead weight carried from the older Android apps. **Not confirmed** — it
  needs a grep across the four consumers, which was out of scope here.
- **`gradle/wrapper/gradle-wrapper.properties`** carries `org.gradle.jvmargs` and a
  2023-dated header alongside `gradle-8.14.5-bin.zip`. Harmless, but it is a settings
  property living in the wrapper file.

## Deliberate exclusions

None. The repo declares no deferred scope. Two documented non-goals are worth recording so
a future run doesn't mistake them for gaps:

- **Maven Central publishing** — deliberately not done; README states the GitHub Packages
  read-auth limitation and names the Central path (`com.vanniktech.maven.publish`) as the
  fix *if* no-auth public consumption is ever wanted. JitPack is explicitly ruled out
  ("does not serve `version-catalog` artifacts cleanly").
- **The three unproven Gradle plugins** — documented as AGP-bound rather than
  Kotlin-ABI-sensitive, needing a real `google-services.json` to test. A reasoned
  deferral, not an oversight.

## Development plan

### Now — correctness debt and one-session fixes

| # | Item | Module (score · axis) | Outcome |
|---|---|---|---|
| 1 | Correct the README import coordinate to `2026.07.25` — the version all four apps actually use | Consumption contract (65% · A1) | The copy-paste onboarding line resolves. One-line fix, highest-consequence bug in the repo. |
| 2 | Remove the hard-coded `2026.07.0` fallback in `build.gradle.kts`; fail the build when `-PcatalogVersion` is missing | Hygiene (40% · A2) | A manual publish can no longer mint a phantom version. Kills the other half of bug #1. |
| 3 | Add `jb-lifecycle-viewmodel` (`version.ref = "jbLifecycle"`) and a `kotlin-jvm` plugin alias to the catalog, then delete the shadow pins from NEPtune-Mobile and DriveSmart | Catalog coverage (70% · A1) | Two apps stop shadowing versions the catalog owns. Restores "one agreed set" for the two libs that broke it. |
| 4 | Document the local-extras reality in README §1 — that the catalog cannot cover every app, with NEPtune-Mobile's remaining extras as the worked example | Consumption contract (65% · A1) | The limitation becomes a stated contract instead of folklore each app rediscovers. |
| 5 | Add a PR-check workflow that builds the catalog (`./gradlew build`) on every PR and tag | Publishing (60% · EN) | A malformed catalog can no longer reach four apps unchallenged. |

### Next (1–2 months)

| # | Item | Module (score · axis) | Outcome |
|---|---|---|---|
| 1 | Add Dependabot or Renovate, or a scheduled workflow that flags pins with newer upstream releases — the single largest hole in the objective's second clause | Hygiene (40% · A2) | Staleness is noticed by the repo rather than by a failing app build. |
| 2 | Fix the version scheme: decide PATCH semantics, re-tag main as `2026.10.0`, and publish so the artifact matches HEAD | Hygiene (40% · A2) | Releases become orderable again and the repo/artifact agree. |
| 3 | Make the tier + rationale reachable from the artifact — e.g. publish `KNOWN-GOOD.md` as a POM-attached doc, or encode tiers in alias names (`unverified-*`) that survive comment stripping | Docs (80% · A2) · Catalog (70% · A1) | A consumer can see that `libs.plugins.ktor` is unproven at the point of use. |
| 4 | Promote or drop the three pre-release pins; build-test the three unproven Gradle plugins in a Gen C app | Catalog (70% · A1) | The shared set stops carrying alpha/rc/beta and untested plugins. |
| 5 | Audit the 15 instrumented-test entries against all four consumers; delete what nobody references | Catalog (70% · A1) | Smaller, honest catalog. |

### Later

- **Verify a Gen D toolchain** (Kotlin 2.4 / AGP 9.x / compileSdk 37). The current notes
  show three Tier-1 pins (`coreKtx`, `lifecycle`, `playPublisher`) held back solely by
  AGP 8.13 + compileSdk 36, and arrow pinned by a Kotlin 2.4 klib ABI. Gen D unblocks all
  four at once. This is the biggest single move available to the fleet.
- **Maven Central publishing**, if no-auth consumption is ever wanted. Revisits a
  documented deferral; the README already names the mechanism.
- **A smoke-resolve consumer** — a throwaway module in CI that imports the just-published
  catalog and compiles for iOS + Android. Closes the Verification dimension for both the
  publishing and consumption modules, which are the two scoring worst on proof.
- **Consider OpenSpec** for this repo. The module list here was derived by hand, so the
  next run reproduces that judgement rather than reading it. Five capability specs would
  make run #2 cheap and genuinely comparable. For an 11-file repo this is optional, not
  obviously worth it — noted, not recommended.

## Method

So a future run reproduces this one.

**Rubric** (fixed, 100 points): Behaviour 50 · Correctness 20 · Verification 20 ·
Reachability 10. Behaviour = `50 × (promises honoured and not contradicted ÷ promises)`,
where a promise that is built but contradicted counts **half**, not zero. Correctness
starts at 20, −7 per High, −5 per Medium, −2 per Low or defect-flagged gap, floor 0.
Verification = `20 × (proven ÷ needing proof)`. Reachability: 10 reachable by consumers ·
5 CLI/tag-triggered only · 0 nothing surfaces it. Scores rounded to the nearest 5.
Rollups weighted by requirement count, never a plain mean:
`overall = Σ(score × reqs) ÷ Σ(reqs)` = 1965 ÷ 31 = 63.4 → **65%**.

**Rubric vocabulary, mapped for a catalog repo.** This is not an application, so the
rubric's terms were translated once and used consistently:

| Rubric term | Here |
|---|---|
| Capability / module | A thing a shared catalog is judged on: the pinned set, its publishing workflow, its consumption contract, its update hygiene, its documentation |
| Requirement | A promise the repo makes about itself in `README.md`, `KNOWN-GOOD.md`, `build.gradle.kts` or `publish.yml` — enumerated per module above, each judged honoured / contradicted / absent |
| Service with a test file | A pin or mechanism with recorded proof: a dated build verification in `KNOWN-GOOD.md`, an exercised CI run, or a consumer that demonstrably uses it |
| Reachable in the UI | Reachable by a consuming app — i.e. actually imported via `from(...)` by one of the four apps |
| Known gap flagged as a defect | A documented limitation that misleads a consumer (stripped tier markers) as opposed to one that merely constrains them (GitHub Packages read-auth) |

**Module list source:** derived by hand from the repo's own stated scope (README sections,
KNOWN-GOOD structure, `build.gradle.kts`, `publish.yml`), per the skill's *No specs?*
path. There is no `openspec/specs/`, so **Behaviour was judged, not counted** from a spec
requirement count.

**Evidence collected:** `git ls-files` (11 files), `git log` (6 commits, 2026-07-25 to
2026-07-26), `git tag -l` (one tag), `git diff v2026.07.25..HEAD` (comment-only changes to
the catalog), `catalog/libs.versions.toml` read in full and cross-checked row-by-row
against `KNOWN-GOOD.md`, `build/version-catalog/libs.versions.toml` compared with its
source for comment stripping (48 → 3), and the `versionCatalogs` block of all four
consuming apps' `settings.gradle.kts` for the real import version and the local extras.

**Not done** (session constraints): no `./gradlew publish`, no `build`, no dependency
resolution, no network version checks, no Artifact published, nothing committed.
Consequently every currency claim is marked `unverifiable offline`.

**Honesty notes.** The repo keeps no status table to be contradicted by, so no
documentation-versus-code drift of that kind exists. The one place where documentation and
reality disagree is the import coordinate, and both sides are reported above. No score in
this report was taken from the repo's own prose: `KNOWN-GOOD.md`'s "**All green**" claim
was accepted as *evidence of a past verification* (it carries dates and names the exact
Gradle tasks) but not as evidence of *current* compatibility, which cannot be checked
offline.
