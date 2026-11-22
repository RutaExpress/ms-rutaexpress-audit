package ruta_express.ms_audit.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ruta_express.ms_audit.dto.AuditEventDTO;
import ruta_express.ms_audit.dto.ShipmentEventDTO;
import ruta_express.ms_audit.service.AuditService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

// test unitario con Mockito de los listeners de Kafka de audit
// reciben el JSON TAL CUAL lo publica shipments (texto, sin headers de tipo) y lo pasan al service ya convertido
@ExtendWith(MockitoExtension.class)
class AuditListenerTest {

    // dependencia simulada (servicio de auditoría)
    @Mock
    private AuditService auditService;

    // clase que se prueba: con un conversor JSON real
    private AuditListener listener;

    // datos: lo que publica shipments en audit.timeline al CREAR un envío (fromStatus null)
    private static final String TIMELINE_JSON = """
            {"eventId":"aud-1","type":"AUDIT_STATUS_CHANGED","timestamp":"2026-11-21T10:00:00Z",
             "traceId":"trace-1","correlationId":"corr-1",
             "payload":{"shipmentId":42,"trackingNumber":"RX-20261121-ABC123","actorId":"oid-cliente",
                        "action":"CREACION","fromStatus":null,"toStatus":"CREADO",
                        "source":"ms-rutaexpress-shipments"}}
            """;

    // datos: lo que publica shipments en shipments.events en la misma operación
    private static final String SHIPMENT_JSON = """
            {"eventId":"shp-1","type":"SHIPMENT_CREADO","timestamp":"2026-11-21T10:00:00Z",
             "traceId":"trace-1","correlationId":"corr-1",
             "payload":{"shipmentId":42,"trackingNumber":"RX-20261121-ABC123","serviceId":"svc-1",
                        "customerId":"oid-cliente","previousStatus":null,"newStatus":"CREADO",
                        "createdAt":"2026-11-21T10:00:00Z"}}
            """;

    // antes de cada test: el listener con el mock y un JsonMapper real
    @BeforeEach
    void setUp() {
        listener = new AuditListener(auditService, JsonMapper.builder().build());
    }

    // test 1 (contrato 3): el JSON de audit.timeline llega convertido en AuditEventDTO
    @Test
    @DisplayName("JSON de audit.timeline: llega convertido al service")
    void timelineJsonReachesService() {
        // act (actuar)
        listener.onAuditTimeline(TIMELINE_JSON);

        // assert (revisar)
        ArgumentCaptor<AuditEventDTO> captor = ArgumentCaptor.forClass(AuditEventDTO.class);
        verify(auditService).processAuditTimeline(captor.capture());
        AuditEventDTO evento = captor.getValue();
        assertEquals("corr-1", evento.correlationId());
        assertEquals("oid-cliente", evento.payload().actorId());
        assertEquals("CREACION", evento.payload().action());
        assertNull(evento.payload().fromStatus());
    }

    // test 2 (contrato 3): el JSON de shipments.events llega convertido en ShipmentEventDTO
    @Test
    @DisplayName("JSON de shipments.events: llega convertido al service")
    void shipmentJsonReachesService() {
        // act (actuar)
        listener.onShipmentEvent(SHIPMENT_JSON);

        // assert (revisar)
        ArgumentCaptor<ShipmentEventDTO> captor = ArgumentCaptor.forClass(ShipmentEventDTO.class);
        verify(auditService).processShipmentEvent(captor.capture());
        assertEquals("corr-1", captor.getValue().correlationId());
        assertEquals("svc-1", captor.getValue().payload().serviceId());
    }

    // test 3 (caso de error): un mensaje que no es JSON -> excepción (el error handler lo manda a audit.DLT)
    @Test
    @DisplayName("Mensaje que no es JSON: excepción y no llega al service")
    void invalidJsonThrows() {
        assertThrows(JacksonException.class, () -> listener.onAuditTimeline("esto no es json"));
        verifyNoInteractions(auditService);
    }
}
