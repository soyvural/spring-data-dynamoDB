#!/usr/bin/env bash

# This script is used to build and push docker image for backend API.

# exit immediately when an error occurs.
set -e

BUILD_VERSION=latest

GIT_ROOT=$(git rev-parse --show-toplevel)

echo "container build with version $BUILD_VERSION"

cd $GIT_ROOT

DOCKER_BUILDKIT=1 docker build -f $GIT_ROOT/Dockerfile -t soyvural/spring-data-dynamodb:"$BUILD_VERSION" .
echo $DOCKER_PASSWORD | docker login registry-1.docker.io --username $DOCKER_USERNAME --password-stdin
docker push docker.io/soyvural/spring-data-dynamodb:"$BUILD_VERSION"