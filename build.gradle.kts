plugins {
    `version-catalog`
    `maven-publish`
}

group = "io.github.gowthamraj07"
// Calendar-versioned: YYYY.MM.PATCH — bump PATCH for same-month revisions.
version = "2026.07.0"

catalog {
    versionCatalog {
        from(files("catalog/libs.versions.toml"))
    }
}

publishing {
    publications {
        create<MavenPublication>("catalog") {
            artifactId = "cmp-catalog"
            from(components["versionCatalog"])
        }
    }
    // Default: publish to GitHub Packages. For Maven Central (truly public,
    // no consumer auth) swap this for the vanniktech maven-publish plugin.
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/gowthamraj07/mobile-deps")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
