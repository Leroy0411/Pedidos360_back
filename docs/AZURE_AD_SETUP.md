# Configuración Azure AD (Entra ID) — Pedidos360

Guía para registrar la aplicación en tu tenant y conectar backend + frontend.

## Datos que necesitarás anotar

Al terminar, copia estos 3 valores:

| Variable | Dónde encontrarla |
|----------|-------------------|
| **Tenant ID** | Entra ID → Información general → Id. de directorio |
| **Client ID** | App registrations → Pedidos360 → Application (client) ID |
| **Audience** | Será `api://{Client ID}` |

---

## Paso 1 — Registrar la aplicación

1. Entra a [Azure Portal](https://portal.azure.com)
2. **Microsoft Entra ID** → **App registrations** → **New registration**
3. Completa:
   - **Name:** `Pedidos360`
   - **Supported account types:** *Accounts in this organizational directory only* (single tenant)
   - **Redirect URI:** plataforma **Single-page application (SPA)** → `http://localhost:4200`
4. Clic en **Register**

---

## Paso 2 — Authentication

1. En la app **Pedidos360** → **Authentication**
2. Verifica la URI SPA: `http://localhost:4200`
3. En **Advanced settings**:
   - **Allow public client flows:** No (MSAL usa Authorization Code + PKCE)
4. **Save**

Opcional (producción): agrega otra URI SPA con la URL de tu frontend desplegado.

---

## Paso 3 — Exponer la API (scope para el backend)

1. **Expose an API** → **Set** junto a Application ID URI
2. Acepta el valor por defecto: `api://{client-id}` → **Save**
3. **Add a scope**:
   - Scope name: `access_as_user`
   - Who can consent: **Admins and users**
   - Admin consent display name: `Access Pedidos360 API`
   - Admin consent description: `Allows the app to access Pedidos360 backend`
   - State: **Enabled**
4. **Add scope**

El scope completo quedará: `api://{client-id}/access_as_user`

---

## Paso 4 — Optional claims (roles en el frontend)

Para que Angular muestre el badge ADMIN sin decodificar el access token manualmente:

1. **App registrations** → **Pedidos360** → **Manifest**
2. Agrega (o fusiona) en el JSON:

```json
"optionalClaims": {
  "idToken": [
    { "name": "roles", "source": null, "essential": false }
  ],
  "accessToken": [
    { "name": "roles", "source": null, "essential": false }
  ]
}
```

3. **Save**

> El backend siempre valida roles desde el **access token** del request.

---

## Paso 5 — Roles de aplicación

1. **App registrations** → **Pedidos360** → **App roles** → **Create app role**

**Rol USER:**
- Display name: `USER`
- Allowed member types: **Users/Groups**
- Value: `USER`
- Description: `Usuario estándar de Pedidos360`

**Rol ADMIN:**
- Display name: `ADMIN`
- Allowed member types: **Users/Groups**
- Value: `ADMIN`
- Description: `Administrador de Pedidos360`

---

## Paso 6 — Permisos Microsoft Graph

1. **API permissions** → **Add a permission**
2. **Microsoft Graph** → **Delegated permissions**
3. Agrega: `User.Read`, `openid`, `profile`, `email`
4. **Grant admin consent for [tenant]** (botón azul)

---

## Paso 7 — Usuarios de prueba y asignación de roles

### Crear usuarios

1. **Microsoft Entra ID** → **Users** → **New user**
2. Crea al menos:
   - `usuario@test.cl` → rol **USER**
   - `admin@test.cl` → rol **ADMIN**

### Asignar roles a la app

1. **Enterprise applications** → busca **Pedidos360**
2. **Users and groups** → **Add user/group**
3. Asigna cada usuario con su rol (USER o ADMIN)

> Los roles aparecen en el claim `roles` del **access token** cuando solicitas el scope `api://{client-id}/access_as_user`.

---

## Paso 8 — Registro de usuarios (self-service)

Para que usuarios **se registren solos** (requerido en EP2):

1. **External Identities** → **Self-service sign up**
2. Activa **Enable user flow for local accounts**
3. O usa un **User flow** de registro/inicio de sesión y vincúlalo a la app

Alternativa en tenant académico: el docente crea usuarios y tú los usas en la demo.

---

## Paso 9 — Conectar el backend

Copia `.env.example` a `.env` en `pedidos360-backend/`:

```env
AZURE_TENANT_ID=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
AZURE_CLIENT_ID=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
AZURE_ISSUER_URI=https://login.microsoftonline.com/{TENANT_ID}/v2.0
AZURE_API_AUDIENCE=api://{CLIENT_ID}
CORS_ORIGINS=http://localhost:4200
```

Inicia el BFF exportando variables (PowerShell):

```powershell
cd pedidos360-backend
Get-Content .env | ForEach-Object {
  if ($_ -match '^([^#=]+)=(.*)$') { Set-Item -Path "env:$($matches[1])" -Value $matches[2] }
}
mvn -pl bff-service spring-boot:run
```

---

## Paso 10 — Conectar el frontend

Edita `pedidos360-frontend/src/environments/environment.development.ts`:

```typescript
tenantId: 'TU-TENANT-ID',
clientId: 'TU-CLIENT-ID',
apiScope: 'api://TU-CLIENT-ID/access_as_user',
```

---

## Verificación rápida

1. Inicia microservicios + BFF + Angular
2. Login con `usuario@test.cl` → debe ver productos y crear pedidos
3. Login con `admin@test.cl` → además puede confirmar pedidos
4. Llamada sin token → **401**
5. Token válido sin rol → **403** en rutas ADMIN

### Probar token manualmente

En DevTools → Application → Local Storage → busca tokens MSAL, copia `accessToken` y prueba:

```bash
curl -H "Authorization: Bearer {token}" http://localhost:8080/api/products
```

---

## Troubleshooting

| Error | Causa probable | Solución |
|-------|----------------|----------|
| `AADSTS50011` redirect URI mismatch | URI incorrecta en Azure | Agrega `http://localhost:4200` como SPA |
| 401 invalid audience | Token para Graph, no para tu API | Scope debe ser `api://{client-id}/access_as_user` |
| 403 sin roles | Usuario sin rol asignado | Asignar rol en Enterprise applications |
| CORS error | Origen no permitido | Verificar `CORS_ORIGINS` en BFF |
