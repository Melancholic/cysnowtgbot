FROM bellsoft/liberica-openjdk-alpine:17
VOLUME /tmp
ARG JAR_FILE
ARG ENTRY_POINT
ARG GPX_ANIMATOR_VER
ARG GPX_FILE_FOR_FORECAST
EXPOSE 3000/tcp
ENV LANG en_US.utf8
RUN echo "https://dl-cdn.alpinelinux.org/alpine/v3.12/main" >  /etc/apk/repositories \
    && echo "https://dl-cdn.alpinelinux.org/alpine/v3.12/community" >> /etc/apk/repositories

RUN apk update \
    && apk upgrade --available && sync \
    && apk add ca-certificates libva-intel-driver \
    && apk add --no-cache tzdata \
    && apk add wget \
    && rm -rf /var/cache/

COPY ${JAR_FILE} cy-snow-telegram-bot.jar
COPY ${ENTRY_POINT} entry_point.sh
COPY ${GPX_FILE_FOR_FORECAST} gpx_for_forecast.gpx

ENTRYPOINT ["sh", "./entry_point.sh"]
