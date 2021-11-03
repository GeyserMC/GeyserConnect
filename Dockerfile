FROM openjdk:16-alpine
RUN mkdir /gsc
WORKDIR /gsc
EXPOSE 19132/udp
CMD ["java", "-Xms1G", "-jar", "GeyserConnect.jar"]