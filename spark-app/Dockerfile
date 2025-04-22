FROM openjdk:11
WORKDIR /app

COPY build/libs/app.jar .
COPY .env .

CMD ["java", "-jar", "app.jar", "spark"]
