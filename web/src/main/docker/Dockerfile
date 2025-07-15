FROM openjdk:8u322-jdk-buster
VOLUME /tmp
#ADD apache-skywalking-java-agent-8.11.0.tgz .
#RUN sh -c 'mv skywalking-agent agent'
ADD ch-sso-*-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'
#RUN apk add --update font-adobe-100dpi ttf-dejavu fontconfig
#RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
#RUN echo 'Asia/Shanghai' >/etc/timezone
ENV JAVA_OPTS=""
#
ENV AGENT_OPTS=""
#-javaagent:/agent/skywalking-agent.jar

EXPOSE 8080
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS $AGENT_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
