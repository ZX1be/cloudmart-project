FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/cloudmart-product-1.0.0-exec.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]
