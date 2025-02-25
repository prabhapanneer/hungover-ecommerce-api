#!/bin/bash

set -x

if [[ -f './duplicate_build' ]]; then
  exit
fi

if [[ $BRANCH_NAME == "Development" ]]; then
  ARTIFACT_NAME=$(./gradlew -q getVersion)
  sudo rm -f ~/.kube/config
  sudo cp /etc/k8s_configs/dev ~/.kube/config && sudo chmod 755 ~/.kube/config
  kubectl set image deployment iao-$IMAGE_REPO_NAME \
    iao-$IMAGE_REPO_NAME=mcgbams-docker.jfrog.io/$IMAGE_REPO_NAME:$ARTIFACT_NAME
  ./build-utils/scripts/routes.sh -s $IMAGE_REPO_NAME -v $ARTIFACT_NAME
else
  echo "Branch $BRANCH_NAME is not autodeployable, please deploy" \
    "$IMAGE_REPO_NAME:$BRANCH_NAME manually"
fi
