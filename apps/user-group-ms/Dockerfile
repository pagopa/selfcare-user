# syntax=docker/dockerfile:1.6@sha256:ac85f380a63b13dfcefa89046420e1781752bab202122f8f50032edf31be0021
FROM maven:3-eclipse-temurin-17@sha256:0d328fa6843bb26b60cf44d69833f241ffe96218fb29fa19df7a6603863eaae7 AS builder

WORKDIR /src
COPY --link pom.xml .

WORKDIR /src/test-coverage
COPY --link ./test-coverage/pom.xml .

WORKDIR /src/apps
COPY --link ./apps/pom.xml .

WORKDIR /src/apps/user-group-ms
COPY --link ./apps/user-group-ms/pom.xml .
COPY ./apps/user-group-ms/ ./

RUN echo "<settings>\n" \
         "<servers>\n" \
         "<server>\n" \
         "<id>\${repositoryId}</id>\n" \
         "<username>\${repoLogin}</username>\n" \
         "<password>\${repoPwd}</password>\n" \
         "</server>\n" \
         "<server>\n" \
         "<id>\${selfcareRepositoryId}</id>\n" \
         "<username>\${repoLogin}</username>\n" \
         "<password>\${repoPwd}</password>\n" \
         "</server>\n" \
         "</servers>\n" \
         "</settings>\n" > settings.xml

ARG REPO_ONBOARDING
ARG REPO_USERNAME
ARG REPO_PASSWORD
ARG REPO_SELFCARE

RUN mvn --global-settings settings.xml --projects :user-group-ms -DrepositoryId=${REPO_ONBOARDING} -DselfcareRepositoryId=${REPO_SELFCARE} -DrepoLogin=${REPO_USERNAME} -DrepoPwd=${REPO_PASSWORD} --also-make-dependents clean package -DskipTests


FROM openjdk:17-jdk@sha256:528707081fdb9562eb819128a9f85ae7fe000e2fbaeaf9f87662e7b3f38cb7d8 AS runtime

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

WORKDIR /app

COPY --from=builder /src/apps/user-group-ms/target/*.jar ./app.jar

ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.2.11/applicationinsights-agent-3.2.11.jar ./applicationinsights-agent.jar
RUN chmod 755 ./applicationinsights-agent.jar

EXPOSE 8080
USER 1001

ENTRYPOINT ["java", "-jar", "app.jar"]