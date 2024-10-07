FROM azul/zulu-openjdk-alpine:17

RUN apk add --no-cache curl

WORKDIR /opt/open-dgr

COPY ./dgrv4_Gateway_serv/build/libs/opendgr-*.jar opendgr.jar

ENTRYPOINT [ "java", "-jar" ]
CMD ["-Xms2g","-Xmx4g", "/opt/open-dgr/opendgr.jar" ]



