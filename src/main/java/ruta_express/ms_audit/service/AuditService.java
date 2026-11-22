package ruta_express.ms_audit.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ruta_express.ms_audit.dto.AuditEntryResponse;
import ruta_express.ms_audit.dto.AuditEventDTO;
import ruta_express.ms_audit.dto.ShipmentEventDTO;
import ruta_express.ms_audit.model.AuditEntry;
import ruta_express.ms_audit.repository.AuditEntryRepository;

// reglas de la auditoría: une los 2 eventos de cada operación en una sola fila y entrega el timeline
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    // acciones que usa shipments (KafkaTopics de shipments)
    private static final String CREACION = "CREACION";
    private static final String CAMBIO_ESTADO = "CAMBIO_ESTADO";

    private final AuditEntryRepository repository;

    // processAuditTimeline (procesar audit.timeline): agrega quién, qué y desde dónde
    @Transactional
    public void processAuditTimeline(AuditEventDTO event) {
        AuditEventDTO.Payload p = event.payload();
        AuditEntry fila = filaDe(event.correlationId(), event.eventId(), event.traceId(), p.shipmentId(),
                p.trackingNumber(), p.fromStatus(), p.toStatus(), event.timestamp());
        if (fila.getAuditEventId() != null) {
            log.info("[AUDIT] audit.timeline repetido, se ignora: {}", event.eventId());
            return;
        }
        fila.setAuditEventId(event.eventId());
        fila.setActorId(p.actorId());
        fila.setAction(p.action());
        fila.setSource(p.source());
        log.info("[AUDIT] envío {}: {} por {}", p.shipmentId(), p.action(), p.actorId());
        repository.save(fila);
    }

    // processShipmentEvent (procesar shipments.events): agrega los datos del envío
    @Transactional
    public void processShipmentEvent(ShipmentEventDTO event) {
        ShipmentEventDTO.Payload p = event.payload();
        AuditEntry fila = filaDe(event.correlationId(), event.eventId(), event.traceId(), p.shipmentId(),
                p.trackingNumber(), p.previousStatus(), p.newStatus(), event.timestamp());
        if (fila.getShipmentEventId() != null) {
            log.info("[AUDIT] shipments.events repetido, se ignora: {}", event.eventId());
            return;
        }
        fila.setShipmentEventId(event.eventId());
        fila.setCustomerId(p.customerId());
        fila.setServiceId(p.serviceId());
        if (fila.getAction() == null) {          // si audit.timeline aún no llega, la acción se deduce
            fila.setAction(p.previousStatus() == null ? CREACION : CAMBIO_ESTADO);
        }
        log.info("[AUDIT] envío {} pasa a {}", p.shipmentId(), p.newStatus());
        repository.save(fila);
    }

    // getTimeline (obtener timeline): los pasos del envío como DTO
    public List<AuditEntryResponse> getTimeline(Long shipmentId) {
        return repository.findByShipmentIdOrderByEventTimestampAsc(shipmentId).stream()
                .map(AuditEntryResponse::from)
                .toList();
    }

    // filaDe (fila de la operación): la que ya existe o una nueva con los datos comunes de los 2 eventos
    // si dos eventos crean la fila a la vez, correlationId es único: uno falla y el reintento la encuentra
    private AuditEntry filaDe(String correlationId, String eventId, String traceId, Long shipmentId,
            String trackingNumber, String desde, String hacia, Instant cuando) {
        String operacion = correlationId != null ? correlationId : eventId;   // sin correlationId: el evento solo
        return repository.findByCorrelationId(operacion).orElseGet(() -> AuditEntry.builder()
                .correlationId(operacion)
                .traceId(traceId)
                .shipmentId(shipmentId)
                .trackingNumber(trackingNumber)
                .fromStatus(desde)
                .toStatus(hacia)
                .eventTimestamp(cuando)
                .build());
    }
}
