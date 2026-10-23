# Pedidos360 — guía de despliegue controlado (no ejecutada)

Estas instrucciones son para **AWS Academy Learner Lab / us-east-1**. No ejecutar cambios sobre la EC2 original antes de revisar su memoria, los servicios y las bases de datos. Se recomienda subir los nuevos JAR primero a un directorio separado y probar sin detener el despliegue previo.

## 1. Inventario de solo lectura, en AWS CloudShell

```bash
aws sts get-caller-identity
aws ec2 describe-instances --region us-east-1 --query 'Reservations[].Instances[].[InstanceId,State.Name,InstanceType,PublicIpAddress]' --output table
aws apigatewayv2 get-apis --region us-east-1 --query 'Items[].[ApiId,Name,ApiEndpoint]' --output table
aws cognito-idp list-user-pools --max-results 20 --region us-east-1
aws rds describe-db-instances --region us-east-1 --query 'DBInstances[].[DBInstanceIdentifier,DBInstanceStatus,Engine]' --output table
```

Si CloudShell no tiene suficientes permisos para alguna consulta, continuar con las otras. **No publicar credenciales ni copiar tokens en chats**.

## 2. Verificar dentro de la EC2 existente (SSH / Session Manager)

```bash
uname -a
free -h
df -h
java -version
docker --version
docker compose version
sudo systemctl --type=service --state=running
sudo ss -lntp
```

Si no existe Docker, solicitar instrucciones para la distribución exacta antes de instalarlo. Dos brokers RabbitMQ pueden consumir memoria considerable. Confirmar capacidad y presupuesto; el clúster en una sola EC2 no ofrece alta disponibilidad real.

## 3. Subir los archivos de deploy a la EC2

Copiar `docker-compose.rabbitmq.yml`, `rabbitmq.conf`, `rabbitmq.env.example` a `~/pedidos360/deploy` usando el método de transferencia ya autorizado de tu laboratorio. Revisar el contenido antes de copiar.

```bash
cd ~/pedidos360/deploy
cp rabbitmq.env.example .env
chmod 600 .env
```

Editar **localmente en EC2** `.env` con tres valores únicos; no copiar al chat ni subir a GitHub. El cookie Erlang debe ser idéntico para los dos nodos. Mantener los puertos de RabbitMQ y Management enlazados a `127.0.0.1`.

## 4. Levantar el clúster (EC2)

```bash
cd ~/pedidos360/deploy
docker compose -f docker-compose.rabbitmq.yml config --quiet
docker compose -f docker-compose.rabbitmq.yml up -d
docker compose -f docker-compose.rabbitmq.yml ps
docker exec $(docker compose -f docker-compose.rabbitmq.yml ps -q rabbit1) rabbitmqctl cluster_status
docker exec $(docker compose -f docker-compose.rabbitmq.yml ps -q rabbit2) rabbitmqctl cluster_status
```

Ambos `cluster_status` deben mostrar `rabbit@rabbit1` y `rabbit@rabbit2`. Si solo muestran un nodo, **detener y diagnosticar la formación del clúster** antes de continuar; no asumir éxito.

## 5. Compilar la nueva copia de backend (en máquina con Java 17+ y Maven)

```bash
cd pedidos360-backend
mvn clean verify
mvn -pl orders-service -am package -DskipTests
mvn -pl rabbit-admin-service -am package -DskipTests
```

El primer comando debe ejecutar las pruebas. El clúster RabbitMQ no es necesario para las pruebas unitarias que usan mocks.

## 6. Arrancar servicios nuevos en EC2, sin reemplazar la versión anterior

Transferir los JAR de `orders-service/target` y `rabbit-admin-service/target` a un directorio separado. Configurar `RABBITMQ_ADDRESSES=localhost:5672`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` y variables de base de datos del perfil actual. No usar contraseñas de ejemplo.

**Importante:** el orders-service no puede ejecutarse en el mismo puerto 8081 que la versión anterior. Verificar los puertos ocupados antes de elegir puertos de prueba y documentar cualquier cambio de `ORDERS_SERVICE_URL` del BFF. No cambiar systemd ni API Gateway hasta que las pruebas den resultado.

## 7. Evidencias EP3/EP4

- Dos nodos en `rabbitmqctl cluster_status` y `docker compose ps`.
- Consola RabbitMQ local mediante túnel SSH a `localhost:15672`: 3 colas + 3 DLQ, exchanges direct/topic, bindings.
- Crear pedido desde API autorizada; observar logs de los tres consumidores.
- Publicar un mensaje de prueba deliberadamente inválido (`bad-event`) en una cola **principal**, y demostrar que llega a su DLQ.
- Comprobar ACK/NACK con los tests y logs; mostrar REST del administrador con nombre válido / inválido y sin autenticación.
- Mostrar 401 sin JWT, 200 con JWT válido y llamada Angular a API Gateway, cuando se complete migración de Cognito.

## 8. Avisos y pendientes

- El ZIP recibido tiene autenticación Azure AD/MSAL. **No desplegar el BFF/frontend del ZIP como si estuvieran migrados a Cognito.** La migración será la siguiente fase.
- La configuración cloud de orders/products apunta a PostgreSQL; la configuración normal usa H2 volátil. Identificar la base real antes de cambiar perfiles.
- Un clúster de dos nodos no garantiza replicación del contenido de las colas clásicas. Las colas declaradas son durables, pero se debe hacer copia y pruebas de recuperación antes de pretender producción.
- La publicación actual ocurre después de guardar en la base de datos: sin outbox transaccional existe una ventana de inconsistencia si falla RabbitMQ. Para el alcance académico se documenta; para producción se recomienda outbox.
- No añadir `deploy/.env`, archivos secretos, logs, `target/` o `node_modules/` al repositorio.
