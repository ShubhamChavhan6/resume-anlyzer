FROM openjdk:17

WORKDIR /app

COPY . .

RUN chmod +x mvnw || true
RUN ./mvnw clean package || mvn clean package

CMD ["java","-jar","target/resume-anlyzer.jar"]