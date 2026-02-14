#!/bin/bash
set -e

NEW_VERSION=$1

# build.gradle.kts
sed -i "s/version = \".*\"/version = \"$NEW_VERSION\"/" build.gradle.kts

# application.yml
sed -i "s/version: v[0-9]\+\.[0-9]\+\.[0-9]\+/version: v$NEW_VERSION/" src/main/resources/application.yml

git config user.name "github-actions[bot]"
git config user.email "github-actions[bot]@users.noreply.github.com"
git add build.gradle.kts src/main/resources/application.yml
git commit -m "chore(release): update version to $NEW_VERSION [skip ci]" || echo "No changes to commit"

