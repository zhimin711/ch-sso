FROM openjdk:8u322-jdk-buster
VOLUME /tmp
ADD ch-sso-1.0.0-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'
#RUN apk add --update font-adobe-100dpi ttf-dejavu fontconfig
#RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
#RUN echo 'Asia/Shanghai' >/etc/timezone
ENV JAVA_OPTS=""

EXPOSE 8080
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
