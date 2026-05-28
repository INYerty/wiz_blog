FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package \
    && JAR_FILE=$(ls -1 target/*.jar | grep -v 'original-' | head -n 1) \
    && cp "$JAR_FILE" /app/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app.jar /app/app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
