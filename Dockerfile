FROM bellsoft/liberica-runtime-container:jre-25.0.3_11-musl
VOLUME /tmp
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
