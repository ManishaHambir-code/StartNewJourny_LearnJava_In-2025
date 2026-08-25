FROM maven:3.8.6-openjdk-8 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM openjdk:8-jre-alpine
RUN addgroup -S ocm && adduser -S ocm -G ocm
WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/app.jar
USER ocm
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=ocm
ENTRYPOINT ["java","-jar","/app/app.jar"]
