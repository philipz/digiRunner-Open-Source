FROM azul/zulu-openjdk-alpine:21-jre-headless-latest

RUN set -eux; \
    apk update; \
    apk upgrade; \
    apk add --no-cache curl; \
    mkdir -p /app; \
    rm -rf /var/cache/apk/*;

COPY ./dgrv4_Gateway_serv/build/libs/* /opt/digirunner/
COPY ./dgrv4_Gateway_serv/keys/* /opt/digirunner/keys/
RUN mv /opt/digirunner/digiRunner-*.jar /opt/digirunner/digirunner.jar

CMD [ "java", "-cp", "/opt/digirunner/digirunner.jar", \
      "-Xms2g", \
      "-Xmx4g", \
      "-DdigiRunner.token.key-store.path=/opt/digirunner/keys", \
      "-Dserver.port=18080", \
      "org.springframework.boot.loader.launch.PropertiesLauncher" \
    ]