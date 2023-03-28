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
