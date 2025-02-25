#!/bin/bash

set -x

ARTIFACT_NAME=$BRANCH_NAME
REV_AUTHOR=$(git show -s --format='%ae')

if [[ $REV_AUTHOR == *"svc_iao"* ]]; then
  touch ./duplicate_build
  exit
fi

if [[ $BRANCH_NAME == "Development" ]]; then
  git checkout Development
  ./gradlew incrementVersion
  git config --global user.email "svc_iao@non-prod.core.bankofamericamerchant.com"
  git config --global user.name "svc_iao"
  git commit -am "[BUILD]: autoincrement version"
  git push origin Development
  ARTIFACT_NAME=$(./gradlew -q getVersion)
fi

./gradlew clean build buildDockerfile

cd build
echo "Building and pushing $IMAGE_REPO_NAME:$ARTIFACT_NAME"
docker build -t $IMAGE_REPO_NAME:$ARTIFACT_NAME .
docker tag $IMAGE_REPO_NAME:$ARTIFACT_NAME $ARTIFACTORY_ENDPOINT/$IMAGE_REPO_NAME:$ARTIFACT_NAME
docker push $ARTIFACTORY_ENDPOINT/$IMAGE_REPO_NAME:$ARTIFACT_NAME
