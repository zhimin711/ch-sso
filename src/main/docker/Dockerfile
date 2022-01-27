FROM java:openjdk-8-alpine2
VOLUME /tmp
ADD ch-sso-1.0.0-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'
# RUN apk add --update font-adobe-100dpi ttf-dejavu fontconfig
ENV JAVA_OPTS="-Dnacos.config.server-addr=192.168.199.194:8848 -Dnacos.config.namespace=d9c61a97-f53c-49a7-ac26-7aae03e08c1a"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
