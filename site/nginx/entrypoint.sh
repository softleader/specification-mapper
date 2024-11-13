#!/bin/sh
envsubst '$STARTER_URL $STARTER_FANS_URL' < /tmp/nginx.default.conf.template > /tmp/nginx.default.conf
cat /tmp/nginx.default.conf
nginx -g "daemon off;"
