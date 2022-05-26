FROM openjdk:17
COPY build/libs/*.jar bot.jar
ENTRYPOINT java -jar bot.jar
EXPOSE 8080