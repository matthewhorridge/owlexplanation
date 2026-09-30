# Releasing OWL Explanation

Releases are built from the `version5` branch and published to Maven Central by
GitHub Actions. The process is intentionally explicit so that the published
artifacts can always be traced to a reviewed commit and immutable tag.

## Prerequisites

The repository must contain these GitHub Actions secrets:

- `GPG_PRIVATE_KEY`
- `GPG_PASSPHRASE`
- `OSSRH_USERNAME`
- `OSSRH_TOKEN`

Despite their OSSRH-prefixed names, `OSSRH_USERNAME` and `OSSRH_TOKEN` contain
Maven Central Portal credentials. The associated Central account must have
permission to publish the `net.sourceforge.owlapi` namespace.

Before starting, confirm that the intended version has not already been
published and that the `version5` branch is passing CI.

## 1. Prepare the release version

Create a branch from the latest `version5` branch:

```bash
git switch version5
git pull --ff-only
git switch -c release-5.0.1
```

The `--ff-only` option stops instead of creating an unexpected merge commit if
the local and remote branches have diverged.

Set the release version:

```bash
mvn --batch-mode versions:set \
  -DnewVersion=5.0.1 \
  -DgenerateBackupPoms=false
```

Review the diff and confirm that it contains only the intended version change.
The release version must not end in `-SNAPSHOT`.

Run the complete build and test suite. This also checks that the release
profile can create the binary, source, and Javadoc artifacts without signing
them locally:

```bash
mvn --batch-mode --activate-profiles release \
  --define release.signing.disabled=true clean verify
```

## 2. Merge the release-version pull request

Commit and push the version change:

```bash
git add pom.xml
git commit -m "Set version to 5.0.1"
git push --set-upstream origin release-5.0.1
```

Open a pull request into `version5`. **Review the diff, wait for all CI checks
to pass, and then merge it.**

## 3. Tag the release

Update the local `version5` branch after merging the pull request:

```bash
git switch version5
git pull --ff-only
```

Confirm that the project version is the intended release version:

```bash
mvn help:evaluate -Dexpression=project.version -q -DforceStdout
```

Create a signed, annotated tag on that commit and push it. **The tag must be
exactly the same string as the version in `pom.xml`.** Do not prefix it with
`v`.

```bash
git tag -s 5.0.1 -m "Release OWL Explanation 5.0.1"
git push origin 5.0.1
```

Pushing the tag automatically starts the publishing workflow. Do not trigger
the workflow manually for a release; manual runs are reserved for snapshots.

## 4. Verify publication

Follow the publishing job in GitHub Actions. Wait for it to succeed, then
confirm that `net.sourceforge.owlapi:owlexplanation:5.0.1` is available from
Maven Central before announcing the release.

Create a GitHub release from the existing `5.0.1` tag. Do not create another
tag in the GitHub release form.

## 5. Start the next development version

Create another branch from `version5` and set the next snapshot version:

```bash
git switch version5
git pull --ff-only
git switch -c start-5.0.2-development
mvn --batch-mode versions:set \
  -DnewVersion=5.0.2-SNAPSHOT \
  -DgenerateBackupPoms=false
```

Commit the change and merge it into `version5` through a pull request.

## Publishing a snapshot

To publish a snapshot, manually run the **Publish packages to the Maven Central
Repository** workflow and select the `version5` branch. The selected branch
must contain a version ending in `-SNAPSHOT`; otherwise, the workflow fails
without publishing anything.

## If publication fails

- If the failure is temporary, rerun the failed tag-triggered workflow.
- If validation fails, check that the tag and `pom.xml` versions match exactly.
- Never move or reuse a tag for a published version. Maven Central artifacts
  are immutable, and the moved tag would no longer identify their source.
- If any artifacts were published, prepare a new version rather than trying to
  overwrite them.
