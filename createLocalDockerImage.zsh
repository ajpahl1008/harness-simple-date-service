#!/bin/zsh

if [ -z "$1" ]; then
  echo "Error: Not enough arguments"
  echo "Usage: createLocalDockerImage.zsh <version>"
  exit 1;
fi

if ! ls build/libs/simple-date-service-*.jar > /dev/null 2>&1; then
  echo "Error: no jar in build/libs. Run ./gradlew build first."
  exit 1;
fi

docker buildx build --platform linux/arm64 --no-cache --load --tag bespinengineering/simple-date-service:${1} .

echo "Build Complete: bespinengineering/simple-date-service:${1} "
