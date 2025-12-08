#!/bin/bash
echo "=== CONFIGURANDO NGINX ==="

# Configurar proxy reverso para Spring Boot
sudo cat > /etc/nginx/conf.d/myapp.conf << 'EOF'
server {
    listen 80;
    server_name ec2-100-31-141-60.compute-1.amazonaws.com;

    # Logs
    access_log /var/log/nginx/myapp_access.log;
    error_log /var/log/nginx/myapp_error.log;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Para Swagger UI
    location /swagger-ui/ {
        proxy_pass http://localhost:8080/swagger-ui/;
        proxy_set_header Host $host;
    }

    # Para API docs
    location /api-docs {
        proxy_pass http://localhost:8080/api-docs;
        proxy_set_header Host $host;
    }

    # Para actuator
    location /actuator/ {
        proxy_pass http://localhost:8080/actuator/;
        proxy_set_header Host $host;
    }
}
EOF

# Verificar configuración
if sudo nginx -t; then
    # Habilitar e iniciar Nginx
    sudo systemctl enable nginx
    sudo systemctl restart nginx
    echo "✅ Nginx configurado correctamente"
    echo "📡 Proxy configurado: puerto 80 → 8080"
else
    echo "❌ Error en configuración de Nginx"
    exit 1
fi