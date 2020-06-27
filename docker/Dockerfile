FROM openjdk:8-jre-slim
RUN mkdir /gsc
WORKDIR /gsc
EXPOSE 19132/udp
EXPOSE 19133/udp
CMD ["java", "-Xms1G", "-jar", "GeyserConnect.jar"]