# Guía de Implementación: Autenticación OAuth con Flutter

## Resumen de Cambios Realizados

Se ha implementado un sistema de autenticación basado en tokens JWT para permitir que tu aplicación Flutter se autentique con el backend Spring Boot usando Google Sign-In.

---

## 🔧 Cambios en el Backend (Spring Boot)

### 1. **Nueva Dependencia en `pom.xml`**
Se agregó la biblioteca de Google API Client para verificar tokens de Google:
```xml
<dependency>
    <groupId>com.google.api-client</groupId>
    <artifactId>google-api-client</artifactId>
    <version>2.2.0</version>
</dependency>
```

### 2. **Nuevo Servicio: `GoogleTokenVerifier.java`**
- **Ubicación**: `src/main/java/com/ieti/proyectoieti/services/GoogleTokenVerifier.java`
- **Función**: Verifica la validez de los tokens ID de Google que envía Flutter
- **Métodos principales**:
  - `verify(String idToken)`: Verifica y extrae información del token
  - `isValid(String idToken)`: Verifica si el token es válido

### 3. **Nuevo Filtro: `JwtAuthenticationFilter.java`**
- **Ubicación**: `src/main/java/com/ieti/proyectoieti/config/JwtAuthenticationFilter.java`
- **Función**: Intercepta todas las peticiones HTTP y valida el token Bearer en el header Authorization
- **Comportamiento**: 
  - Si el token es válido, autentica al usuario
  - Si el token es inválido o no existe, continúa sin autenticación

### 4. **Actualización: `SecurityConfig.java`**
- Cambió de `SessionCreationPolicy.IF_REQUIRED` a `SessionCreationPolicy.STATELESS`
- Agregó el `JwtAuthenticationFilter` antes del filtro de autenticación estándar
- Permitió acceso sin autenticación a `/api/auth/verify`

### 5. **Actualización: `AuthController.java`**
Se agregaron nuevos endpoints:

#### **`POST /api/auth/verify`**
- Recibe el ID token de Google desde Flutter
- Verifica el token
- Crea o actualiza el usuario en la base de datos
- Retorna el perfil del usuario

```json
// Request
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
}

// Response (éxito)
{
  "authenticated": true,
  "user": {
    "id": "60a7c1b5f3b4c7001f8e4c3d",
    "name": "Juan Pérez",
    "email": "juan@example.com",
    "picture": "https://lh3.googleusercontent.com/...",
    "providerUserId": "123456789",
    "groupIds": [],
    "createdAt": "2024-01-15T10:30:00"
  }
}

// Response (error)
{
  "error": "Invalid token"
}
```

#### **`GET /api/user/profile`**
- Ahora funciona con autenticación JWT (Bearer token)
- Extrae información del usuario del token validado

### 6. **Nuevo DTO: `TokenRequest.java`**
- **Ubicación**: `src/main/java/com/ieti/proyectoieti/controllers/dto/TokenRequest.java`
- Clase para recibir el token en el endpoint `/api/auth/verify`

---

## 📱 Cambios Necesarios en Flutter

### 1. **Actualizar el Flujo de Autenticación**

Tu código Flutter actual ya está casi listo. Solo necesitas hacer estos ajustes:

#### **Modificar `_handleGoogleSignIn` en `AuthService`**

```dart
Future<void> _handleGoogleSignIn(GoogleSignInAccount account) async {
  print('📱 [AuthService] Handling Google Sign In for: ${account.email}');
  _googleUser = account;
  
  try {
    print('🔑 [AuthService] Getting authentication token...');
    final GoogleSignInAuthentication auth = await account.authentication;
    final String? idToken = auth.idToken;
    
    print('🔑 [AuthService] ID Token: ${idToken != null ? "✓ Present" : "✗ Missing"}');
    
    if (idToken == null) {
      throw Exception('No ID token received from Google');
    }

    // CAMBIO: Primero verifica el token con el backend
    print('🌐 [AuthService] Verifying token with backend...');
    final response = await http.post(
      Uri.parse('${ApiService.baseUrl}/api/auth/verify'),
      headers: {'Content-Type': 'application/json'},
      body: json.encode({'idToken': idToken}),
    );

    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      print('✅ [AuthService] Token verified successfully');
      
      // Set the token for future requests
      ApiService.setAuthToken(idToken);
      
      // Extract user from response
      final userData = data['user'];
      _currentUser = User.fromJson(userData);
      print('✅ [AuthService] User profile loaded: ${_currentUser?.name}');
      
      _isAuthenticated = true;
      _isGuest = false;
    } else {
      throw Exception('Token verification failed: ${response.statusCode}');
    }
  } catch (e) {
    print('❌ [AuthService] Error handling Google Sign In: $e');
    rethrow;
  }
}
```

### 2. **El `ApiService` ya está correcto**

Tu implementación actual del `ApiService` ya incluye el soporte para Bearer tokens:

```dart
static Map<String, String> _getHeaders() {
  final headers = {
    'Content-Type': 'application/json',
  };
  
  if (_authToken != null) {
    headers['Authorization'] = 'Bearer $_authToken';
  }
  
  return headers;
}
```

Esto es perfecto. El token se enviará automáticamente en todas las peticiones.

---

## 🚀 Pasos para Probar

### 1. **Compilar el Backend**

```bash
mvn clean install
```

### 2. **Ejecutar el Backend**

```bash
mvn spring-boot:run
```

### 3. **Configurar Google Cloud Console**

Asegúrate de tener configurado:

#### Para Android:
- SHA-1 de tu keystore de debug/release
- Package name de tu app

#### Para iOS:
- Bundle ID de tu app
- iOS URL scheme

#### Para Web:
- Authorized JavaScript origins: `http://localhost:puerto`
- Authorized redirect URIs: `http://localhost:puerto`

### 4. **Configurar el `application.properties`**

```properties
spring.security.oauth2.client.registration.google.client-id=TU_CLIENT_ID.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=TU_CLIENT_SECRET
```

### 5. **Probar el Flujo**

1. El usuario hace clic en "Sign in with Google" en Flutter
2. Flutter muestra el diálogo de Google Sign-In
3. El usuario selecciona su cuenta
4. Flutter recibe el `idToken`
5. Flutter envía el `idToken` a `/api/auth/verify`
6. El backend verifica el token con Google
7. El backend crea/actualiza el usuario en la base de datos
8. El backend retorna el perfil del usuario
9. Flutter guarda el `idToken` y lo usa en todas las peticiones subsiguientes

---

## 🔐 Flujo de Autenticación

```
[Flutter App]
    |
    | 1. User clicks "Sign in with Google"
    v
[Google Sign-In]
    |
    | 2. User authenticates
    v
[Flutter receives idToken]
    |
    | 3. POST /api/auth/verify with idToken
    v
[Spring Boot Backend]
    |
    | 4. Verify token with Google
    v
[Google Token Verification]
    |
    | 5. Token valid, extract user info
    v
[Create/Update User in MongoDB]
    |
    | 6. Return user profile
    v
[Flutter stores token]
    |
    | 7. All subsequent requests include:
    |    Authorization: Bearer <idToken>
    v
[JwtAuthenticationFilter]
    |
    | 8. Verify token on every request
    v
[Protected Resources]
```

---

## ⚠️ Consideraciones Importantes

### 1. **Expiración de Tokens**
Los ID tokens de Google expiran después de 1 hora. Deberás implementar:
- Renovación automática del token en Flutter
- Manejo de errores 401 para refrescar el token

### 2. **Seguridad**
- Nunca compartas el `client-secret` en el código de Flutter
- El `client-secret` solo debe estar en el backend
- Los tokens se transmiten siempre por HTTPS en producción

### 3. **CORS**
La configuración actual permite:
```java
"http://localhost:3000"
"http://localhost:8080"
"https://*.github.io"
```

Para Flutter web, asegúrate de que el origen esté permitido.

### 4. **Endpoints Públicos**
Los siguientes endpoints NO requieren autenticación:
- `/api/auth/status`
- `/api/auth/verify`
- `/health`
- `/swagger-ui/**`

Todos los demás endpoints requieren el token Bearer.

---

## 🐛 Solución de Problemas

### Error: "Invalid token"
- Verifica que el `client-id` en `application.properties` coincida con el de Google Cloud Console
- Asegúrate de que el token no haya expirado
- Verifica que la fecha/hora del servidor esté correcta

### Error: "User not authenticated" en peticiones
- Verifica que el header `Authorization: Bearer <token>` esté presente
- Revisa que `ApiService.setAuthToken(token)` se haya llamado después del login

### Error de CORS
- Agrega el origen de tu app Flutter a `corsConfigurationSource()` en `SecurityConfig.java`

---

## 📚 Recursos Adicionales

- [Google Sign-In para Flutter](https://pub.dev/packages/google_sign_in)
- [Google Identity: Verify ID Tokens](https://developers.google.com/identity/sign-in/web/backend-auth)
- [Spring Security con JWT](https://spring.io/guides/tutorials/spring-boot-oauth2/)

---

## ✅ Checklist de Implementación

- [x] Agregar dependencia `google-api-client` al `pom.xml`
- [x] Crear `GoogleTokenVerifier.java`
- [x] Crear `JwtAuthenticationFilter.java`
- [x] Actualizar `SecurityConfig.java` para autenticación stateless
- [x] Crear endpoint `/api/auth/verify` en `AuthController.java`
- [x] Crear `TokenRequest.java` DTO
- [ ] Actualizar `_handleGoogleSignIn` en Flutter para llamar `/api/auth/verify`
- [ ] Probar el flujo completo de autenticación
- [ ] Configurar Google Cloud Console con los SHA-1 y Bundle IDs correctos
- [ ] Configurar `application.properties` con las credenciales de Google
- [ ] Implementar renovación automática de tokens en Flutter
- [ ] Probar en Android, iOS y Web

---

## 💡 Próximos Pasos Recomendados

1. **Implementar Refresh Token**: Para mantener la sesión activa sin requerir login frecuente
2. **Agregar Rate Limiting**: Proteger el endpoint `/api/auth/verify` contra ataques
3. **Logging y Monitoreo**: Registrar intentos de autenticación fallidos
4. **Tests Unitarios**: Crear tests para `GoogleTokenVerifier` y `JwtAuthenticationFilter`
5. **Manejo de Errores**: Mejorar los mensajes de error para debugging
