# Pedidos360 Backend

Backend del sistema **Pedidos360** (DSY1107 — Desarrollo Cloud Native I).

Arquitectura de **microservicios Spring Boot** con **BFF** (Backend for Frontend) que valida JWT emitido por **Azure AD** (IDaaS).

## Estructura

```
pedidos360-backend/
├── common-security/    # Validación JWT compartida (issuer, audience, firma, roles)
├── bff-service/        # :8080 — API pública, JWT + autorización por rol
├── orders-service/     # :8081 — CRUD de pedidos (JPA + H2/PostgreSQL)
└── products-service/   # :8082 — Catálogo de productos (JPA + H2/PostgreSQL)
```

## Requisitos

- Java 17+
- Maven 3.9+
- Tenant Azure AD configurado (roles `USER` y `ADMIN`)

## Configuración Azure AD

1. Registrar la **API** en el tenant y exponer scope `access_as_user`.
2. Definir roles de aplicación: `USER`, `ADMIN`.
3. Asignar roles a usuarios de prueba.
4. Copiar `.env.example` y completar:

```properties
AZURE_ISSUER_URI=https://login.microsoftonline.com/{tenant-id}/v2.0
AZURE_API_AUDIENCE=api://{client-id}
AZURE_CLIENT_ID={client-id}
```

## Ejecución local

Abrir **3 terminales** en la raíz del proyecto:

```bash
# Terminal 1 — productos
mvn -pl products-service spring-boot:run

# Terminal 2 — pedidos
mvn -pl orders-service spring-boot:run

# Terminal 3 — BFF (requiere JWT válido en /api/**)
mvn -pl bff-service spring-boot:run
```

Compilar todo y ejecutar tests:

```bash
mvn clean verify
```

## Endpoints del BFF (protegidos con JWT)

| Método | Ruta | Rol |
|--------|------|-----|
| GET | `/api/products` | USER, ADMIN |
| GET | `/api/products/{id}` | USER, ADMIN |
| POST | `/api/products` | ADMIN |
| GET | `/api/orders` | USER, ADMIN |
| GET | `/api/orders/{id}` | USER, ADMIN |
| POST | `/api/orders` | USER, ADMIN |
| PUT | `/api/orders/{id}/confirm` | ADMIN |
| GET | `/health` | Público |

## Validación JWT en el BFF

El módulo `common-security` implementa:

- Verificación de **firma** vía JWKS de Azure AD
- Validación de **issuer** (`iss`)
- Validación de **audience** (`aud`)
- Validación de **expiración** (`exp`)
- Extracción de **roles** desde el claim `roles`
- Respuestas **401** (sin token / token inválido) y **403** (sin rol)

## Despliegue cloud (EC2 + API Gateway)

- Activar perfil `cloud`: `SPRING_PROFILES_ACTIVE=cloud`
- Configurar variables `DB_*` para PostgreSQL
- El **API Gateway** enruta al BFF y puede revalidar JWT
- CORS: configurar `CORS_ORIGINS` con la URL del frontend Angular

## Integración con frontend Angular (MSAL)

El frontend debe enviar el access token en cada llamada:

```
Authorization: Bearer {access_token}
```

Audience del token debe coincidir con `AZURE_API_AUDIENCE`.

## Próximos pasos

- [ ] Conectar tenant Azure AD real
- [ ] Desplegar en EC2
- [ ] Configurar AWS API Gateway con rutas y JWT authorizer
- [ ] Frontend Angular con MSAL + MsalInterceptor
