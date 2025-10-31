FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn

RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

RUN apk add --no-cache \
      curl \
      bash

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV TZ=America/Bogota

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]