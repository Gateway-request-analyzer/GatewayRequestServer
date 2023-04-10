# 1st Docker build stage: build the project with Maven
FROM maven:3.6.3-openjdk-11 as builder
WORKDIR /project
COPY . /project/
RUN mvn clean package -DskipTests -B

# 2nd Docker build stage: copy builder output and configure entry point
FROM adoptopenjdk:11-jre-hotspot
ENV APP_DIR /application
ENV APP_FILE gra-server.jar
ENV PUBLIC_KEY public_key.pem

EXPOSE 3000

WORKDIR $APP_DIR
COPY --from=builder /project/target/*-fat.jar $APP_DIR/$APP_FILE
COPY --from=builder /project/public_key.pem $APP_DIR/$PUBLIC_KEY

ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $APP_FILE"]
