FROM amazoncorretto:23.0.1-alpine
WORKDIR /app
COPY target/tracking-system-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
