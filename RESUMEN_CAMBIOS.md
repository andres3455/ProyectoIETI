# Resumen de Cambios para Autenticación OAuth con Flutter

## 📋 Cambios Realizados en el Backend

### Archivos Nuevos Creados ✨

1. **`GoogleTokenVerifier.java`**
   - Servicio para verificar tokens ID de Google
   - Valida la autenticidad del token con los servidores de Google
   - Ubicación: `src/main/java/com/ieti/proyectoieti/services/`

2. **`JwtAuthenticationFilter.java`**
   - Filtro que intercepta todas las peticiones HTTP
   - Extrae y valida el token Bearer del header Authorization
   - Ubicación: `src/main/java/com/ieti/proyectoieti/config/`

3. **`TokenRequest.java`**
   - DTO para recibir el token en el endpoint de verificación
   - Ubicación: `src/main/java/com/ieti/proyectoieti/controllers/dto/`

### Archivos Modificados 🔧

1. **`pom.xml`**
   - ✅ Agregada dependencia: `com.google.api-client:google-api-client:2.2.0`

2. **`SecurityConfig.java`**
   - ✅ Cambiado a autenticación STATELESS
   - ✅ Agregado `JwtAuthenticationFilter`
   - ✅ Permitido acceso público a `/api/auth/verify`

3. **`AuthController.java`**
   - ✅ Nuevo endpoint: `POST /api/auth/verify`
   - ✅ Actualizado: `GET /api/user/profile` (ahora soporta JWT)
   - ✅ Actualizado: `GET /api/auth/status` (ahora soporta JWT)

### Documentación 📚

1. **`OAUTH_FLUTTER_IMPLEMENTATION.md`**
   - Guía completa de implementación
   - Diagrama de flujo de autenticación
   - Solución de problemas
   - Checklist de implementación

2. **`flutter_auth_service_updated.dart`**
   - Código actualizado de AuthService para Flutter
   - Incluye el nuevo flujo de verificación con el backend

---

## 🚀 Qué Hace Diferente Ahora

### ANTES (OAuth Web Tradicional)
```
Flutter → Google Sign-In → Redirect a Backend → Backend crea sesión → Cookie
```
❌ No funciona con aplicaciones móviles/web SPA

### DESPUÉS (OAuth con Tokens JWT)
```
Flutter → Google Sign-In → Obtiene ID Token → 
Envía token a Backend → Backend verifica con Google → 
Retorna perfil de usuario → Flutter usa token en cada petición
```
✅ Funciona perfectamente con Flutter mobile/web

---

## 🔑 Nuevo Endpoint Principal

### `POST /api/auth/verify`

**Descripción**: Verifica el ID token de Google y crea/actualiza el usuario

**Request**:
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE4MmU0NTBhMzVhYzRhOTQ4OTA1MzllMzFmYjc2NjFiMjVhNzUyOTUiLCJ0eXAiOiJKV1QifQ..."
}
```

**Response (Éxito - 200)**:
```json
{
  "authenticated": true,
  "user": {
    "id": "60a7c1b5f3b4c7001f8e4c3d",
    "name": "Juan Pérez",
    "email": "juan@example.com",
    "picture": "https://lh3.googleusercontent.com/a/...",
    "providerUserId": "123456789012345678901",
    "groupIds": [],
    "createdAt": "2024-01-15T10:30:00.000Z"
  }
}
```

**Response (Error - 401)**:
```json
{
  "error": "Invalid token"
}
```

---

## 📱 Cambios Necesarios en Flutter

### 1. Actualizar el método `_handleGoogleSignIn`

**Ubicación**: `lib/services/auth_service.dart`

**CAMBIO CLAVE**: Ahora debes llamar al endpoint `/api/auth/verify` antes de usar el token:

```dart
Future<void> _handleGoogleSignIn(GoogleSignInAccount account) async {
  _googleUser = account;
  
  // Obtener el token ID de Google
  final GoogleSignInAuthentication auth = await account.authentication;
  final String? idToken = auth.idToken;
  
  if (idToken == null) {
    throw Exception('No ID token received from Google');
  }

  // 🆕 NUEVO: Verificar el token con el backend
  final response = await http.post(
    Uri.parse('${ApiService.baseUrl}/api/auth/verify'),
    headers: {'Content-Type': 'application/json'},
    body: json.encode({'idToken': idToken}),
  );

  if (response.statusCode == 200) {
    final data = json.decode(response.body);
    
    // Guardar el token para futuras peticiones
    ApiService.setAuthToken(idToken);
    
    // Extraer el usuario de la respuesta
    _currentUser = User.fromJson(data['user']);
    _isAuthenticated = true;
    _isGuest = false;
  } else {
    throw Exception('Token verification failed');
  }
}
```

### 2. El resto del código permanece igual ✅

Tu `ApiService` ya está correctamente configurado para enviar el token Bearer:

```dart
static Map<String, String> _getHeaders() {
  final headers = {'Content-Type': 'application/json'};
  if (_authToken != null) {
    headers['Authorization'] = 'Bearer $_authToken';
  }
  return headers;
}
```

---

## ⚙️ Configuración Requerida

### 1. Backend - `application.properties`

```properties
# Google OAuth2 Configuration
spring.security.oauth2.client.registration.google.client-id=TU_CLIENT_ID.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=TU_CLIENT_SECRET

# MongoDB Configuration (si aplica)
spring.data.mongodb.uri=mongodb://localhost:27017/tu_base_de_datos
```

### 2. Flutter - Google Sign-In Configuration

#### Android (`android/app/build.gradle`):
```gradle
defaultConfig {
    applicationId "com.tu.paquete"
    // ... otros configs
}
```

#### iOS (`ios/Runner/Info.plist`):
```xml
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>com.googleusercontent.apps.TU_CLIENT_ID_REVERSED</string>
        </array>
    </dict>
</array>
```

#### Web (`web/index.html`):
```html
<meta name="google-signin-client_id" content="TU_CLIENT_ID.apps.googleusercontent.com">
```

### 3. Google Cloud Console

Necesitas configurar:
- ✅ OAuth 2.0 Client ID (Web application)
- ✅ OAuth 2.0 Client ID (Android) con SHA-1
- ✅ OAuth 2.0 Client ID (iOS) con Bundle ID
- ✅ Authorized redirect URIs
- ✅ Authorized JavaScript origins (para web)

---

## 🧪 Cómo Probar

### Paso 1: Compilar el Backend
```bash
mvn clean install
```

### Paso 2: Ejecutar el Backend
```bash
mvn spring-boot:run
```

### Paso 3: Probar el Endpoint con cURL

```bash
curl -X POST http://localhost:8080/api/auth/verify \
  -H "Content-Type: application/json" \
  -d '{"idToken": "TU_TOKEN_DE_GOOGLE"}'
```

### Paso 4: Probar con Flutter

1. Actualiza `AuthService` con el código nuevo
2. Asegúrate de que `ApiService.baseUrl` apunte a tu backend
3. Ejecuta la app y haz clic en "Sign in with Google"
4. Verifica los logs en la consola

---

## 🐛 Solución de Problemas Comunes

### Error: "Invalid token"
**Causa**: El client-id no coincide o el token expiró
**Solución**: 
- Verifica que el client-id en `application.properties` sea el correcto
- Asegúrate de que el token sea reciente (< 1 hora)

### Error: "User not authenticated" en peticiones
**Causa**: El token no se está enviando correctamente
**Solución**:
- Verifica que `ApiService.setAuthToken(idToken)` se llame después del login
- Revisa que `_getHeaders()` incluya el Authorization header

### Error de CORS en Flutter Web
**Causa**: El origen no está permitido en el backend
**Solución**:
```java
// En SecurityConfig.java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:3000",
    "http://localhost:8080",
    "http://localhost:*",  // 🆕 Agregar esto para Flutter web
    "https://*.github.io"
));
```

### Error: "Failed to load user profile" en Flutter
**Causa**: El usuario no existe en la base de datos
**Solución**: El endpoint `/api/auth/verify` debería crear el usuario automáticamente. Verifica los logs del backend.

---

## ✅ Checklist de Implementación

### Backend
- [x] Agregar dependencia `google-api-client` al `pom.xml`
- [x] Crear `GoogleTokenVerifier.java`
- [x] Crear `JwtAuthenticationFilter.java`
- [x] Crear `TokenRequest.java`
- [x] Actualizar `SecurityConfig.java`
- [x] Actualizar `AuthController.java`
- [ ] Configurar `application.properties` con credenciales de Google
- [ ] Compilar y ejecutar el backend
- [ ] Probar el endpoint `/api/auth/verify` con Postman

### Flutter
- [ ] Actualizar `_handleGoogleSignIn` en `AuthService`
- [ ] Verificar que `baseUrl` en `ApiService` sea correcto
- [ ] Configurar Google Sign-In en Android (`build.gradle` y SHA-1)
- [ ] Configurar Google Sign-In en iOS (`Info.plist` y Bundle ID)
- [ ] Configurar Google Sign-In en Web (`index.html`)
- [ ] Probar el flujo completo de login
- [ ] Verificar que las peticiones subsiguientes incluyan el token
- [ ] Implementar manejo de expiración de tokens

### Google Cloud Console
- [ ] Crear OAuth 2.0 Client IDs (Web, Android, iOS)
- [ ] Configurar SHA-1 fingerprint (Android)
- [ ] Configurar Bundle ID (iOS)
- [ ] Configurar Authorized redirect URIs
- [ ] Configurar Authorized JavaScript origins
- [ ] Habilitar Google+ API (si es necesario)

---

## 📊 Diagrama de Flujo Detallado

```
┌─────────────┐
│ Flutter App │
└──────┬──────┘
       │ 1. User clicks "Sign in with Google"
       v
┌──────────────────┐
│ Google Sign-In   │
│ Dialog           │
└──────┬───────────┘
       │ 2. User selects Google account
       v
┌──────────────────┐
│ Google OAuth     │
│ Authentication   │
└──────┬───────────┘
       │ 3. Returns idToken
       v
┌──────────────────┐
│ Flutter App      │
│ receives token   │
└──────┬───────────┘
       │ 4. POST /api/auth/verify
       │    {"idToken": "..."}
       v
┌──────────────────────┐
│ Spring Boot Backend  │
│ JwtAuthenticationFilter (not triggered yet)
└──────┬───────────────┘
       │ 5. AuthController.verifyToken()
       v
┌──────────────────────┐
│ GoogleTokenVerifier  │
│ verify(idToken)      │
└──────┬───────────────┘
       │ 6. Calls Google servers
       v
┌──────────────────┐
│ Google Token     │
│ Verification API │
└──────┬───────────┘
       │ 7. Returns user info (sub, email, name, picture)
       v
┌──────────────────────┐
│ Spring Boot Backend  │
│ UserService          │
└──────┬───────────────┘
       │ 8. createOrUpdateUser()
       v
┌──────────────────┐
│ MongoDB          │
│ Save/Update User │
└──────┬───────────┘
       │ 9. Return user document
       v
┌──────────────────────┐
│ Spring Boot Backend  │
│ AuthController       │
└──────┬───────────────┘
       │ 10. Return JSON response
       │     {"authenticated": true, "user": {...}}
       v
┌──────────────────┐
│ Flutter App      │
│ Stores idToken   │
│ Stores User      │
└──────┬───────────┘
       │
       │ ═══ SUBSEQUENT REQUESTS ═══
       │
       │ 11. Any API call
       │     Authorization: Bearer <idToken>
       v
┌──────────────────────┐
│ Spring Boot Backend  │
│ JwtAuthenticationFilter
└──────┬───────────────┘
       │ 12. Extract token from header
       │     Verify with GoogleTokenVerifier
       v
┌──────────────────────┐
│ GoogleTokenVerifier  │
│ verify(idToken)      │
└──────┬───────────────┘
       │ 13. Token valid?
       │     Yes: Set SecurityContext
       │     No: Continue without auth
       v
┌──────────────────────┐
│ Controller Method    │
│ (UserController,     │
│  GroupController,    │
│  etc.)               │
└──────┬───────────────┘
       │ 14. Process request
       v
┌──────────────────────┐
│ Return Response      │
└──────────────────────┘
```

---

## 🎯 Puntos Clave a Recordar

1. **El token ID de Google es válido por 1 hora** - Necesitarás implementar renovación
2. **El backend NO genera JWT propios** - Usa directamente el ID token de Google
3. **Cada petición debe incluir el token** - El filtro lo valida automáticamente
4. **La autenticación es STATELESS** - No hay sesiones en el servidor
5. **El endpoint `/api/auth/verify` es público** - No requiere autenticación previa
6. **El filtro es transparente** - Si el token no existe o es inválido, la petición continúa pero sin autenticación

---

## 🔮 Mejoras Futuras Recomendadas

1. **Implementar Refresh Tokens**
   - Mantener sesiones activas sin re-login constante
   - Usar `refreshToken` de Google OAuth

2. **Cache de Validación de Tokens**
   - Evitar validar el mismo token múltiples veces
   - Usar Redis o cache en memoria

3. **Rate Limiting**
   - Proteger `/api/auth/verify` contra ataques de fuerza bruta
   - Limitar intentos por IP

4. **Logging y Auditoría**
   - Registrar todos los intentos de autenticación
   - Alertas de seguridad

5. **Tests Unitarios**
   - Tests para `GoogleTokenVerifier`
   - Tests para `JwtAuthenticationFilter`
   - Tests de integración para el flujo completo

6. **Manejo de Múltiples Proveedores**
   - Soportar Apple Sign-In, Facebook, etc.
   - Unificar el flujo de autenticación

---

¿Necesitas ayuda con algún paso específico? 🚀
