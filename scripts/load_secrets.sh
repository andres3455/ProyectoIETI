#!/bin/bash
echo "Cargando secrets para Google OAuth..."

SECRETS_FILE="/home/ec2-user/application-secrets.env"
if [ -f "$SECRETS_FILE" ]; then
    # Cargar solo variables de Google
    GOOGLE_CLIENT_ID=$(grep '^GOOGLE_CLIENT_ID=' "$SECRETS_FILE" | cut -d'=' -f2)
    GOOGLE_CLIENT_SECRET=$(grep '^GOOGLE_CLIENT_SECRET=' "$SECRETS_FILE" | cut -d'=' -f2)

    if [ -n "$GOOGLE_CLIENT_ID" ]; then
        export GOOGLE_CLIENT_ID
        echo "✅ GOOGLE_CLIENT_ID cargado"
    else
        echo "⚠️ GOOGLE_CLIENT_ID no encontrado en secrets"
    fi

    if [ -n "$GOOGLE_CLIENT_SECRET" ]; then
        export GOOGLE_CLIENT_SECRET
        echo "✅ GOOGLE_CLIENT_SECRET cargado"
    else
        echo "⚠️ GOOGLE_CLIENT_SECRET no encontrado en secrets"
    fi
else
    echo "❌ Archivo no encontrado: $SECRETS_FILE"
fi