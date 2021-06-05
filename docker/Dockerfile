FROM openjdk:8-jre-slim
RUN mkdir /gsc
WORKDIR /gsc
EXPOSE 19132/udp
CMD ["java", "-Xms1G", "-jar", "GeyserConnect.jar"]