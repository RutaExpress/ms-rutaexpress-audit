package ruta_express.ms_audit.dto;

import java.time.Instant;

public record AuditEventDTO(
    String eventId,
    String type,
    Instant timestamp,
    String traceId,
    String correlationId,
    Payload payload
) {
    public record Payload(
        Long shipmentId,
        String trackingNumber,
        String actorId,
        String action,
        String fromStatus,
        String toStatus,
        String source
    ) {}
}