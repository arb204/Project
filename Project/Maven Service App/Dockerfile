FROM maven:3.8.4-openjdk-8-slim as maven
COPY ./pom.xml ./pom.xml
COPY ./src ./src
RUN mvn dependency:go-offline -B
RUN mvn package
FROM openjdk:8u171-jre-alpine
WORKDIR /CS1660_Proj
COPY  ./1660-project.jar ./1660_project.jar
CMD ["java", "-jar", "./1660_project.jar"]