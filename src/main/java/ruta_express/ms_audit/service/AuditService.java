package ruta_express.ms_audit.service;

import ruta_express.ms_audit.dto.AuditEventDTO;
import ruta_express.ms_audit.dto.ShipmentEventDTO;
import ruta_express.ms_audit.model.AuditEntry;
import ruta_express.ms_audit.repository.AuditEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEntryRepository repository;

    // Procesa los eventos que vienen del tópico audit.timeline
    @Transactional
    public void processAuditTimeline(AuditEventDTO event) {
        log.info("Procesando audit.timeline: Envío {} - Acción {}", 
                event.payload().shipmentId(), event.payload().action());
        
        AuditEntry entry = AuditEntry.builder()
                .eventId(event.eventId())
                .shipmentId(event.payload().shipmentId())
                .trackingNumber(event.payload().trackingNumber())
                .actorId(event.payload().actorId())
                .action(event.payload().action())
                .fromStatus(event.payload().fromStatus())
                .toStatus(event.payload().toStatus())
                .source(event.payload().source())
                .eventTimestamp(event.timestamp())
                .build();
        
        repository.save(entry);
    }

    // Procesa los eventos que vienen del tópico shipments.events
    @Transactional
    public void processShipmentEvent(ShipmentEventDTO event) {
        log.info("Procesando shipments.events para auditoría: Envío {} - Estado {}", 
                event.payload().shipmentId(), event.payload().newStatus());

        AuditEntry entry = AuditEntry.builder()
                .eventId(event.eventId())
                .shipmentId(event.payload().shipmentId())
                .trackingNumber(event.payload().trackingNumber())
                .action("CAMBIO_ESTADO")
                .fromStatus(event.payload().previousStatus())
                .toStatus(event.payload().newStatus())
                .source("ms-rutaexpress-shipments")
                .eventTimestamp(event.timestamp())
                .build();
        
        repository.save(entry);
    }

    // Obtiene el timeline para el endpoint de solo lectura
    public List<AuditEntry> getTimeline(Long shipmentId) {
        return repository.findByShipmentIdOrderByEventTimestampAsc(shipmentId);
    }
}