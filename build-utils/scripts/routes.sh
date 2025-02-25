#!/bin/bash

set -x

while getopts ":s:v:" opt; do
  case $opt in
  s)
    SERVICE=$OPTARG
    ;;
  v)
    NEW_VERSION=$OPTARG
    ;;
  \?)
    echo "Invalid option: -$OPTARG" >&2
    ;;
  esac
done

URL=https://bams-dev-api-management.azure-api.net/core/api/v1/$SERVICE

# Wait up to 5 minutes for the service to boot
count=0
while [[ $count -lt 20 ]]; do

  version="$(curl -H Ocp-Apim-Subscription-Key:${API_MGMT_SUB_CREDS_PSW} ${URL}/version 2>/dev/null | jq '.version' | tr -d \")"
  echo "Found version ${version} and looking for ${NEW_VERSION}"

  if [[ $version == $NEW_VERSION ]]; then
    cd /usr/local/bin/bams-apim
    node ./lib/auto -e dev -s $SERVICE
    exit 0
  fi

  echo "Waiting 15 seconds for service to update"
  ((count++))
  sleep 15
done

echo "Unable to update routes"
exit 1
