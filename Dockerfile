FROM openjdk:17-jdk

ARG JAR_PATH=build/libs/*.jar
COPY ${JAR_PATH} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
