FROM openjdk:17-alpine
WORKDIR /home/app
COPY build/libs/SuperDac-0.1-all.jar /home/app/app.jar
EXPOSE 80
ENTRYPOINT ["java", "-jar", "/home/app/app.jar"]
