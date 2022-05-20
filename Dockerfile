FROM openjdk:11
COPY build/libs/*.jar backend.jar
ENTRYPOINT java -jar backend.jar
EXPOSE 8080