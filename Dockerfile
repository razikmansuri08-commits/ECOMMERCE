FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

# Fail fast with full error output instead of a truncated tail.
RUN mvn -B -e clean package -DskipTests -Dmaven.test.skip=true

FROM eclipse-temurin:21-jdk


WORKDIR /app

#COPY target/ecom-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]