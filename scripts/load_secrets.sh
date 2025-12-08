#!/bin/bash
echo "Cargando secrets..."

SECRETS_FILE="/home/ec2-user/application-secrets.env"
if [ -f "$SECRETS_FILE" ]; then
    # Cargar variables
    export $(grep -v '^#' "$SECRETS_FILE" | xargs)

    # Configurar redirect URI (IMPORTANTE)
    PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "localhost")
    export SPOTIFY_REDIRECT_URI="http://${PUBLIC_IP}:8080/callback"

    echo "✅ Secrets cargados"
else
    echo "❌ Archivo no encontrado: $SECRETS_FILE"
fi