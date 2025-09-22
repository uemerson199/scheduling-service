FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace/app

COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .
COPY src/ src/

RUN chmod +x ./gradlew && ./gradlew build -x test --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app


COPY --from=build /workspace/app/build/libs/app.jar app.jar


EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
