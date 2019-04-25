FROM gradle:jdk11 as builder

COPY --chown=gradle:gradle . /home/gradle/src/
WORKDIR /home/gradle/src
RUN gradle build -p server -x test

FROM openjdk:11-jre-slim
EXPOSE 8080
COPY --from=builder /home/gradle/src/server/build/libs/server-1-0-SNAPSHOT.jar /app/
WORKDIR /app

CMD ["java","-jar","server-1-0-SNAPSHOT.jar"]