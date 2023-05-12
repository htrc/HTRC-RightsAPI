#!/usr/bin/env bash

if [ "$#" -eq 0 ]; then
  echo "No arguments supplied."
  echo "Usage: bash docker-build.sh dockerImageTag"
  echo "dockerImageTag - The image tag for the docker-registry.htrc.indiana.edu/rights-api image name"
  echo "Examples: bash docker-build.sh dev"
  exit 1
fi

mvn clean install

docker build --no-cache -t docker-registry.htrc.indiana.edu/rights-api:$1 .
docker push docker-registry.htrc.indiana.edu/rights-api:$1