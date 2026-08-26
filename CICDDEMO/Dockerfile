FROM eclipse-temurin:21-jre

LABEL maintainer="TahaShafiq"

WORKDIR /myapp

COPY target/CICDDEMO-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","app.jar"]