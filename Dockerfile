FROM openjdk:8-jre-alpine

ENV APPLICATION_USER authorizer
RUN adduser -D -g '' $APPLICATION_USER

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

USER $APPLICATION_USER
COPY ./build/libs/authorizer-1.0-all.jar /app/authorizer-1.0-all.jar
WORKDIR /app

ENTRYPOINT ["java","-jar","/app/authorizer-1.0-all.jar"]