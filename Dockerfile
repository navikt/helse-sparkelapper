FROM gcr.io/distroless/java21-debian12:nonroot

COPY build/libs/*.jar /app/

ENV TZ="Europe/Oslo"
ENV JAVA_OPTS='-XX:MaxRAMPercentage=75'

WORKDIR /app

CMD ["app.jar"]