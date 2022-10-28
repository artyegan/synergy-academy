FROM amazoncorretto:16

WORKDIR /app

COPY /build/libs/synergy-academy-1.0-SNAPSHOT.jar .

CMD ["java", "-jar", "synergy-academy-1.0-SNAPSHOT.jar"]

EXPOSE 1238