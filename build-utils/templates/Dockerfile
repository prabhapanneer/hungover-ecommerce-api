FROM openjdk:16.0.1-jdk
VOLUME /tmp
ADD libs/${project_name}.war /app.war
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java \$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.war" ]
