## Stage 1: Build JAR file ##
FROM eclipse-temurin:21-jdk as build

WORKDIR /app

## Copy gradle and build configs
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN chmod +x ./gradlew
## Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code to docker
COPY src ./src

## Build a JAR
RUN ./gradlew bootJar -x test --no-daemon

## Stage 2: Runtime stage ##
FROM eclipse-temurin:21-jre as runtime

WORKDIR /app

## Creating an user, because without an user, the container runs as root, not ideal
RUN useradd --system --no-create-home appuser
USER appuser

## Copy the Jar from build stage
COPY --from=build /app/build/libs/*.jar urbannest-2.0.jar

EXPOSE 8080

CMD ["java", "-jar" , "urbannest-2.0.jar"]