FROM openjdk:17
COPY build/libs/healthy-bot-0.0.1.jar app.jar
ENTRYPOINT java -jar app.jar