# Pedidos360 — estado de implementación RabbitMQ (22 octubre 2026)

## Entregado en esta copia

- Módulo `orders-service`: topología con seis colas durables (3 principales y 3 DLQ), exchanges topic/direct y dead-letter direct, y seis bindings. Nombres definidos en `application.yml`.
- Publicador que emite tres mensajes tras crear cada pedido (uno por caso de uso).
- Tres consumidores de dominio que reciben por `@RabbitListener`, con ACK manual para éxito y NACK sin requeue para error. Un mensaje con formato inválido sirve para demostrar su DLQ.
- Módulo `rabbit-admin-service` nuevo, con endpoints REST para crear y eliminar colas, exchanges y bindings; DTO validados, API de administración encapsulada y HTTP Basic en localhost.
- Docker Compose para dos brokers con discovery de clúster, puertos locales y volúmenes separados.
- Tests unitarios para productor, ACK y DLQ, y tests web para validación y seguridad de la API administrativa.
- Guía CloudShell/EC2 en `deploy/GUIA_AWS_CLOUDSHELL.md`.

## Alcance real

Los consumidores de notificaciones y documentos **registran procesamiento simulado**; no envían email ni generan PDF. Esto sirve para observar en la demo la mensajería desacoplada, pero si el profesor exige artefactos funcionales habrá que implementar esas salidas.

La publicación ocurre después del commit de JPA, no hay patrón outbox. Si RabbitMQ falla, un pedido puede quedar guardado sin eventos. Para producción sería necesario outbox, reintentos persistentes y pruebas de carga/recuperación.

La guía de AWS **no fue ejecutada**; la capacidad real de la EC2, la base de datos y los recursos del laboratorio siguen sin verificar.

## Estado de pruebas

Pruebas agregadas al código. No ejecutadas en este entorno porque Maven y Docker no estaban disponibles. Se requiere `mvn clean verify` antes de dar por compilado el backend. Tampoco se ha validado una ejecución real de los dos nodos.

## Siguiente fase

Migrar de Azure AD/MSAL a Cognito en BFF/frontend y corregir roles, endpoints y callback OAuth. Esto no está incluido como cambio funcional en esta entrega para evitar mezclar una migración de seguridad no probada con RabbitMQ.
