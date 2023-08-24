FROM amazoncorretto:17
WORKDIR /app
COPY . /app
RUN chmod +x ./gradlew
RUN ./gradlew build -x test
CMD ["java", "-jar", "-Dspring.profiles.active=postgres", "build/libs/commerce-0.0.1-SNAPSHOT.jar"]