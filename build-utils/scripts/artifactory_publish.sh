#!/usr/bin/env bash

REV_AUTHOR=$(git show -s --format='%ae')

if [[ $BRANCH_NAME == "Development" ]]; then
  if [[ $REV_AUTHOR == *"svc_iao"* ]]; then
    touch ./duplicate_build
    exit
  fi

  git checkout Development

  ./gradlew incrementVersion
  git config --global user.email "svc_iao@non-prod.core.bankofamericamerchant.com"
  git config --global user.name "svc_iao"
  git commit -am "[BUILD]: autoincrement version"
  git push origin Development

  ./gradlew artifactoryPublish -Drepo=release
else
  ./gradlew snapshotVersion
  ./gradlew artifactoryPublish -Drepo=snapshot
fi
