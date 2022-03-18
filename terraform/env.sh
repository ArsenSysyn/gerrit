#!/bin/bash
export IP_CACHE=$(terraform output -raw cacheip)
export APP_NAME=$(terraform output -raw appname)
export APP_ENV_NAME=$(terraform output -raw envname)
export IP_APP=$(terraform output -raw eip)

