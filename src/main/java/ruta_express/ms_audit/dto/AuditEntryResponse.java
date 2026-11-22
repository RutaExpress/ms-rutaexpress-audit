package ruta_express.ms_audit.dto;

import java.time.Instant;

import ruta_express.ms_audit.model.AuditEntry;

// lo que devuelve /api/v1/audit/shipments/{id}: un paso del timeline (quién, qué, cuándo y desde dónde)
// la entidad AuditEntry nunca sale por la API (convención de capas)
public record AuditEntryResponse(
        String correlationId,     // la operación (une los 2 eventos de shipments)
        Long shipmentId,
        String trackingNumber,
        String action,            // qué: CREACION o CAMBIO_ESTADO
        String fromStatus,        // null en la creación
        String toStatus,
        String actorId,           // quién: oid del usuario
        String source,            // desde dónde: ms-rutaexpress-shipments
        String customerId,        // dueño del envío
        String serviceId,         // servicio del catálogo
        Instant eventTimestamp    // cuándo
) {

    // from (desde): convierte la entidad en la respuesta
    public static AuditEntryResponse from(AuditEntry e) {
        return new AuditEntryResponse(e.getCorrelationId(), e.getShipmentId(), e.getTrackingNumber(), e.getAction(),
                e.getFromStatus(), e.getToStatus(), e.getActorId(), e.getSource(), e.getCustomerId(),
                e.getServiceId(), e.getEventTimestamp());
    }
}
