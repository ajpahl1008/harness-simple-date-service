#!/bin/zsh

if [ -z "$1" ]; then
  echo "Error: Not enough arguments"
  echo "Usage: runDockerImage.zsh <version>"
  exit 1;
fi

# The service needs no configuration to run, so .env is optional here
env_args=()
if [ -f .env ]; then
  env_args=(--env-file .env)
fi

echo "Deleting any previous version of simple-date-service "

docker stop simple-date-service 2>/dev/null
docker rm simple-date-service 2>/dev/null

echo "Running bespinengineering/simple-date-service:${1} "

docker run \
  -p 8080:8080 \
  --name simple-date-service \
  $env_args \
  bespinengineering/simple-date-service:${1}
