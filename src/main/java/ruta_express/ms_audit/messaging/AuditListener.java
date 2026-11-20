package ruta_express.ms_audit.messaging;

import ruta_express.ms_audit.config.KafkaTopics;
import ruta_express.ms_audit.dto.AuditEventDTO;
import ruta_express.ms_audit.dto.ShipmentEventDTO;
import ruta_express.ms_audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditListener {

    private final AuditService auditService;

    // Escucha el tópico exclusivo de auditoría (con groupId = audit)
    @KafkaListener(topics = KafkaTopics.AUDIT_TIMELINE, groupId = "audit")
    public void onAuditTimeline(AuditEventDTO event) {
        log.info("Mensaje recibido en audit.timeline");
        auditService.processAuditTimeline(event);
    }

    // Escucha el tópico de envíos para registrar los cambios de estado (mismo groupId = audit)
    @KafkaListener(topics = KafkaTopics.SHIPMENTS_EVENTS, groupId = "audit")
    public void onShipmentEvent(ShipmentEventDTO event) {
        log.info("Mensaje recibido en shipments.events para auditoría");
        auditService.processShipmentEvent(event);
    }
}