## How to create a new CIFuzz Gradle plugin release

The process for creating a new cifuzz release is automated.

### Step 1: Create a version tag
First of all, make sure you are on the latest version of the `main` branch:

    git checkout main
    git pull

(Optional) Check what tags already exist:

    git tag

Create a new tag with a new version number, e.g.:

    git tag v1.0.1 -m "Version 1.0.1"

Please make sure to prefix version tags with a `v` as shown above.

### Step 2: Push the version tag to trigger the release pipeline
Push the new tag to origin:

    git push origin main --tags

Pushing a version tag will trigger an automatic [release pipeline](https://github.com/CodeIntelligenceTesting/cifuzz-gradle-plugin/actions/workflows/release.yml) on GitHub.

The pipeline automatically releases the plugin to the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.code-intelligence.cifuzz).
In case there are issues, you can log into the Gradle Plugin Portal with the Code Intelligence account and manage the released versions there.

## How to handle a new Gradle release

If a new Gradle version is released, do the following to update the plugin build and test with the additional Gradle version:

- Assuming the new version is `8.1`, run the following command **twice**: `./gradlew wrapper --gradle-version=8.1`
- The plugin now builds and tests against the new version. Make sure the tests are still passing: `./gradlew test`
- Consider adding the **previous** version to the list of `testedGradleVersions` in [build.gradle.kts](build.gradle.kts).
