FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY ./target/service-comment.jar service-payment.jar
ENTRYPOINT ["java","-jar","/service-payment.jar", "&"]