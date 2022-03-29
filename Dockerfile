FROM maven:3.8.4-jdk-11-slim AS MAVEN_BUILD
MAINTAINER Toby Peel
COPY pom.xml /build/
COPY src /build/src/
WORKDIR /build/
RUN mvn clean package -DskipTests

FROM openjdk:11-jdk-slim
WORKDIR /app
COPY --from=MAVEN_BUILD /build/target/sggcws-1.1.0.jar /app/
ENTRYPOINT ["java","-jar","sggcws-1.1.0.jar"]