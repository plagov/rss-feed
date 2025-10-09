FROM bellsoft/liberica-runtime-container:jre-24.0.2_12-musl
VOLUME /tmp
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
