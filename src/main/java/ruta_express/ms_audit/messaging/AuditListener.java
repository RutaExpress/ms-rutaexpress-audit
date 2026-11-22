package ruta_express.ms_audit.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ruta_express.ms_audit.config.KafkaTopics;
import ruta_express.ms_audit.dto.AuditEventDTO;
import ruta_express.ms_audit.dto.ShipmentEventDTO;
import ruta_express.ms_audit.service.AuditService;
import tools.jackson.databind.ObjectMapper;

// consumidores de Kafka de audit (group audit): reciben el JSON como texto y lo convierten aquí
// shipments publica sin headers de tipo, así que cada método decide a qué clase convertirlo
@Component
@RequiredArgsConstructor
public class AuditListener {

    // dependencias: el service y el conversor JSON de Spring
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    // onAuditTimeline (al llegar a audit.timeline): JSON -> AuditEventDTO -> service
    @KafkaListener(topics = KafkaTopics.AUDIT_TIMELINE, groupId = "audit")
    public void onAuditTimeline(String json) {
        auditService.processAuditTimeline(objectMapper.readValue(json, AuditEventDTO.class));
    }

    // onShipmentEvent (al llegar a shipments.events): JSON -> ShipmentEventDTO -> service
    @KafkaListener(topics = KafkaTopics.SHIPMENTS_EVENTS, groupId = "audit")
    public void onShipmentEvent(String json) {
        auditService.processShipmentEvent(objectMapper.readValue(json, ShipmentEventDTO.class));
    }
}
