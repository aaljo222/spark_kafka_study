FROM openjdk:17
WORKDIR /app

COPY build/libs/app.jar app.jar
COPY .env .env  # ← 꼭 복사해야 함

CMD ["java", "-jar", "app.jar"]
