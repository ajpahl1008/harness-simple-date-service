FROM amazoncorretto:25-alpine3.22

LABEL org.opencontainers.image.authors="info@bespinengineering.com"
LABEL org.opencontainers.image.title="simple-date-service"
LABEL org.opencontainers.image.description="Returns the current date in ISO-8601 format."

# curl backs the HEALTHCHECK below; the base image does not ship it
RUN apk update && apk add --no-cache curl

# Setup container to run as non-root user
RUN adduser -D -g "Simple Date Service ID" sdg sdg
USER sdg

# Set environment variables (optional)
ENV JAVA_HOME=/usr/lib/jvm/default-jvm
ENV PATH=$PATH:$JAVA_HOME/bin

# Set the working directory in the docker image
WORKDIR /app

# Copy the JAR file into the image. The plain jar is disabled in build.gradle,
# so this glob matches exactly one file.
COPY build/libs/simple-date-service-*.jar app.jar

# Expose the REST API, Swagger UI and actuator
EXPOSE 8080

# Uses the actuator health endpoint, so an unhealthy container is visible to
# Docker and to any orchestrator reading container health
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1

# Run the JAR file with the java command
ENTRYPOINT ["java","-jar","app.jar"]
