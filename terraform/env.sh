#!/bin/bash
export IP_CACHE=$(terraform output -raw cacheip)
export APP_NAME=$(terraform output -raw appname)
export APP_ENV_NAME=$(terraform output -raw envname)
export URL=$(terraform output -raw url)
export REGION=$(terraform output -raw region)

