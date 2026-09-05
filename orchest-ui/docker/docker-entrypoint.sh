#!/bin/sh
set -eu

TEMPLATE=/etc/orchest/runtime-config.js.template
OUTPUT=/runtime-config/runtime-config.js

VARS="VITE_BASEPATH VITE_AI_API_URL VITE_BPMN_PROMPT_BASEPATH VITE_BYPASS_LOGIN VITE_SSO_CLIENT_ID VITE_MICROSOFT_AUTHORITY VITE_SSO_TENANT_ID VITE_REDIRECT_URI VITE_SSO_LOGOUT_REDIRECT_URI VITE_ALLOWED_EXTERNAL_DOMAINS VITE_MODE VITE_ENABLE_QUOTE_BANNER VITE_ENGINE_ACTUATOR_URL"

# Render template by substituting ${VAR} placeholders with env var values.
# Pure sh+sed — avoids needing envsubst (gettext) in the base image.
tmp="${OUTPUT}.tmp"
cp "$TEMPLATE" "$tmp"

for var in $VARS; do
  value=$(printenv "$var" 2>/dev/null || true)
  # Escape sed metacharacters: backslash, ampersand, and our delimiter "|"
  escaped=$(printf '%s' "$value" | sed -e 's/[\\&|]/\\&/g')
  sed -i "s|\${$var}|$escaped|g" "$tmp"
done

mv "$tmp" "$OUTPUT"

exec "$@"
