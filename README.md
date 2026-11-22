# ms-rutaexpress-audit

Microservicio de **auditoría** del caso RutaExpress: consume desde **Kafka** `shipments.events` y `audit.timeline` y arma el historial de cada envío (quién creó, aceptó, despachó o entregó). Guarda en **Oracle**.

> Arquitectura en capas (rama `refactor/ArquitecturaCapas`).

## Tecnologías
Java 21 · Spring Boot 4.1.2 · Spring Kafka · Spring Data JPA (Oracle) · Lombok · JUnit 5 y Mockito

## Arquitectura en capas
Un evento entra por **messaging → service → repository**; una consulta baja por **controller → service → repository**.

```
src/main/java/ruta_express/ms_audit/
├── messaging/    Listeners de shipments.events y audit.timeline (grupo «audit»)
├── controller/   Endpoint del timeline
├── service/      Une los dos eventos de cada operación en una fila (por correlationId), sin duplicados
├── repository/   Spring Data JPA
├── model/        Entidad AuditEntry
├── dto/          Eventos recibidos y respuesta
├── exception/    Manejador global (400 si el id no es número)
└── config/       Tópicos y manejo de errores de Kafka (3 reintentos y luego audit.DLT)
```

## Endpoints (puerto 8087)
| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/api/v1/audit/shipments/{id}` | Timeline del envío: acción, estado anterior y nuevo, actor, origen y fecha |

## Tests
```
mvnw.cmd test
```
