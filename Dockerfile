FROM openjdk:11
COPY build/libs/*.jar bot.jar
ENTRYPOINT java -jar bot.jar
EXPOSE 8080