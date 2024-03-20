FROM gcr.io/distroless/java21-debian12:nonroot

COPY build/libs/*.jar /app/

ENV TZ="Europe/Oslo"
ENV JAVA_OPTS='-XX:MaxRAMPercentage=70 -XX:+UseZGC -XX:+ZGenerational'

WORKDIR /app

CMD ["app.jar"]