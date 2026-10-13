#!/bin/sh
# Cloud platforms (Render, Railway, Heroku...) aksar DATABASE_URL dete hain:
#   postgres://user:pass@host:port/dbname   ya   mysql://user:pass@host:port/dbname
# Spring ko jdbc: format chahiye, to yahan automatically convert karte hain.
# Agar DB_URL tumne khud set kiya hai to ye kuch nahi badalta.
if [ -z "$DB_URL" ] && [ -n "$DATABASE_URL" ]; then
  url="$DATABASE_URL"
  case "$url" in
    postgres://*)   scheme="postgresql"; rest="${url#postgres://}" ;;
    postgresql://*) scheme="postgresql"; rest="${url#postgresql://}" ;;
    mysql://*)      scheme="mysql";      rest="${url#mysql://}" ;;
    *) echo "Unsupported DATABASE_URL scheme"; exit 1 ;;
  esac
  creds="${rest%@*}"
  hostpart="${rest##*@}"
  export DB_USER="${creds%%:*}"
  export DB_PASSWORD="${creds#*:}"
  hostpath="${hostpart%%\?*}"
  case "$hostpath" in
    *:*) : ;;
    *) # port nahi diya to default lagao
       host="${hostpath%%/*}"; path="${hostpath#*/}"
       if [ "$scheme" = "postgresql" ]; then hostpath="$host:5432/$path"; else hostpath="$host:3306/$path"; fi ;;
  esac
  if [ "$scheme" = "postgresql" ]; then
    export DB_URL="jdbc:postgresql://${hostpath}?sslmode=require"
  else
    export DB_URL="jdbc:mysql://${hostpath}?sslMode=REQUIRED&serverTimezone=Asia/Kolkata"
  fi
fi
exec java $JAVA_OPTS -jar /app/app.jar
