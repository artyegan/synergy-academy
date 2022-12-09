FROM amazoncorretto:16

ARG JAR_VERSION

ENV JAR_VERSION_ENV=$JAR_VERSION

WORKDIR /app

COPY /build/libs/synergy-academy-$JAR_VERSION_ENV.jar .

CMD java -jar synergy-academy-$JAR_VERSION_ENV.jar

EXPOSE 1238