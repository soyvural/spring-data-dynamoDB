#!/usr/bin/env bash

set -eox pipefail

GIT_ROOT=$(git rev-parse --show-toplevel)
cd $GIT_ROOT/deploy/storage

terraform init;
terraform destroy -auto-approve;
