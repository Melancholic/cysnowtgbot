FROM bellsoft/liberica-openjre-alpine:26

ARG JAR_FILE
ARG ENTRY_POINT
ARG WORK_DIR=/var/cy-snow-telegram-bot

# tzdata: musl ships no zoneinfo db, so TZ (Europe/Moscow) is a no-op without it
# ca-certificates: HTTPS to cyprusski.com, open-meteo and the Telegram API
RUN apk add --no-cache tzdata ca-certificates

ENV LANG=en_US.utf8

RUN addgroup -S -g 1000 app \
    && adduser -S -u 1000 -G app -h /app app

# Pre-created so the bot can write logs/snapshots even when the host dir isn't
# bind-mounted; with a mount, host ownership wins and must match uid 1000.
RUN mkdir -p "$WORK_DIR/logs" && chown -R app:app "$WORK_DIR"

WORKDIR /app
COPY --chown=app:app ${JAR_FILE} cy-snow-telegram-bot.jar
COPY --chown=app:app ${ENTRY_POINT} entry_point.sh

USER app

ENTRYPOINT ["sh", "./entry_point.sh"]
