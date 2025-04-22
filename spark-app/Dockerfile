FROM openjdk:17
WORKDIR /app

COPY build/libs/app.jar .
COPY .env .

CMD ["java", "-jar", "app.jar", "spark"]
