FROM eclipse-temurin:26-jdk

WORKDIR /app

RUN apt-get update \
    && apt-get install -y curl \
    && rm -rf /var/lib/apt/lists/*

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]