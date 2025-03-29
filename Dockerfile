FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy the build files
COPY build.gradle.kts settings.gradle.kts ./

# Copy the source code
COPY src ./src

# Build the application
RUN gradle --no-daemon build

# Create a lightweight runtime image
FROM openjdk:17-slim

WORKDIR /app

# Copy the built application
COPY --from=build /app/build/distributions/Snackbox-1.0.0.tar /app/

# Extract the distribution
RUN tar -xf Snackbox-1.0.0.tar && \
    rm Snackbox-1.0.0.tar && \
    mv Snackbox-1.0.0/* . && \
    rmdir Snackbox-1.0.0

# Create a volume for persistent data
VOLUME /app/data

# Set the entry point
ENTRYPOINT ["./bin/Snackbox"]
