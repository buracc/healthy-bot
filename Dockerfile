FROM openjdk:17
COPY build/libs/*.jar /home/healthybot/bot.jar
WORKDIR /home/healthybot
ENTRYPOINT java -jar bot.jar