FROM gradle:jdk11 as builder

COPY --chown=gradle:gradle . /home/gradle/src/
WORKDIR /home/gradle/src
RUN gradle build -p client -x test

FROM openjdk:11-jre-slim
COPY --from=builder /home/gradle/src/client/build/libs/client-1.0-SNAPSHOT.jar /app/
WORKDIR /app

ENTRYPOINT ["java","-jar","client-1.0-SNAPSHOT.jar"]