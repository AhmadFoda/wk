FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package

EXPOSE 8080
ENTRYPOINT ["java","-jar","target/backend-0.0.1-SNAPSHOT.jar"]
