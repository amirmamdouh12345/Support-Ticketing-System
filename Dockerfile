ARG APP_NAME

FROM gradle:8.7-jdk21 as build-image

ARG APP_NAME
ENV APP_HOME=$APP_NAME

WORKDIR $APP_HOME


# Copy the Gradle config, source code, and static analysis config into the build container.
COPY --chown=gradle:gradle $APP_HOME/build.gradle ${APP_HOME}/settings.gradle ./
COPY --chown=gradle:gradle $APP_HOME/src ./src

# Build the application.

RUN gradle clean bootJar -x test --no-daemon

RUN mv build/libs/*.jar build/libs/app.jar


# Java image for the application to run in.

FROM eclipse-temurin:21-jre-alpine


ARG APP_NAME

WORKDIR $APP_NAME

ENV APP_HOME=$APP_NAME


COPY --from=build-image /home/gradle/${APP_NAME}/build/libs/app.jar ./app.jar



# The command to run when the container starts.
ENTRYPOINT java -jar app.jar
