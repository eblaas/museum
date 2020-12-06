FROM openjdk:13-jdk-alpine

EXPOSE 8080

RUN mkdir -p /data
COPY MetObjects.csv /data/MetObjects.csv
ARG JAR_FILE=build/libs/*.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
