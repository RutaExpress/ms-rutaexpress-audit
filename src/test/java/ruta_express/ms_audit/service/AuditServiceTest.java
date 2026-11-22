package ruta_express.ms_audit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ruta_express.ms_audit.dto.AuditEntryResponse;
import ruta_express.ms_audit.dto.AuditEventDTO;
import ruta_express.ms_audit.dto.ShipmentEventDTO;
import ruta_express.ms_audit.model.AuditEntry;
import ruta_express.ms_audit.repository.AuditEntryRepository;

// test unitario con Mockito del service de auditoría
// shipments publica 2 eventos por operación con el MISMO correlationId (shipments.events y audit.timeline):
// audit guarda UNA sola fila por operación; el primero que llega la crea y el segundo la completa
@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    // dependencia simulada (repositorio de auditoría)
    @Mock
    private AuditEntryRepository repository;

    // clase que se prueba, con el mock inyectado por el constructor
    @InjectMocks
    private AuditService auditService;

    // datos: hora de la operación y su correlationId (igual en los 2 eventos)
    private static final Instant CUANDO = Instant.parse("2026-11-21T10:30:00Z");
    private static final String OPERACION = "corr-1";

    // datos: evento de audit.timeline (quién, qué y desde dónde)
    private AuditEventDTO timeline(String eventId, String accion, String desde, String hacia) {
        return new AuditEventDTO(eventId, "AUDIT_STATUS_CHANGED", CUANDO, "trace-1", OPERACION,
                new AuditEventDTO.Payload(42L, "RX-20261121-ABC123", "oid-operador", accion, desde, hacia,
                        "ms-rutaexpress-shipments"));
    }

    // datos: evento de shipments.events (estado completo del envío)
    private ShipmentEventDTO envio(String eventId, String desde, String hacia) {
        return new ShipmentEventDTO(eventId, "SHIPMENT_" + hacia, CUANDO, "trace-1", OPERACION,
                new ShipmentEventDTO.Payload(42L, "RX-20261121-ABC123", "svc-1", "oid-cliente", desde, hacia, CUANDO));
    }

    // test 1 (HU 9, regla principal): audit.timeline llega primero -> crea la fila con quién, qué y desde dónde
    @Test
    @DisplayName("audit.timeline primero: crea la fila con actor, acción y origen")
    void timelineCreatesEntryWithActor() {
        // arrange (preparar): todavía no hay fila de esta operación
        when(repository.findByCorrelationId(OPERACION)).thenReturn(Optional.empty());

        // act (actuar)
        auditService.processAuditTimeline(timeline("aud-1", "CAMBIO_ESTADO", "EN_BODEGA", "EN_RUTA"));

        // assert (revisar)
        ArgumentCaptor<AuditEntry> captor = ArgumentCaptor.forClass(AuditEntry.class);
        verify(repository).save(captor.capture());
        AuditEntry fila = captor.getValue();
        assertEquals(OPERACION, fila.getCorrelationId());
        assertEquals("aud-1", fila.getAuditEventId());
        assertEquals("oid-operador", fila.getActorId());
        assertEquals("CAMBIO_ESTADO", fila.getAction());
        assertEquals("EN_BODEGA", fila.getFromStatus());
        assertEquals("EN_RUTA", fila.getToStatus());
        assertEquals("ms-rutaexpress-shipments", fila.getSource());
    }

    // test 2 (sin duplicado): shipments.events llega después -> completa la MISMA fila, no crea otra
    @Test
    @DisplayName("shipments.events después: completa la misma fila")
    void shipmentEventCompletesSameEntry() {
        // arrange (preparar): la fila ya existe, creada por audit.timeline
        AuditEntry existente = AuditEntry.builder().correlationId(OPERACION).auditEventId("aud-1")
                .actorId("oid-operador").shipmentId(42L).toStatus("EN_RUTA").build();
        when(repository.findByCorrelationId(OPERACION)).thenReturn(Optional.of(existente));

        // act (actuar)
        auditService.processShipmentEvent(envio("shp-1", "EN_BODEGA", "EN_RUTA"));

        // assert (revisar): se guardó la misma fila, con los datos del envío y sin perder el actor
        ArgumentCaptor<AuditEntry> captor = ArgumentCaptor.forClass(AuditEntry.class);
        verify(repository).save(captor.capture());
        AuditEntry fila = captor.getValue();
        assertSame(existente, fila);
        assertEquals("shp-1", fila.getShipmentEventId());
        assertEquals("oid-cliente", fila.getCustomerId());
        assertEquals("svc-1", fila.getServiceId());
        assertEquals("oid-operador", fila.getActorId());
    }

    // test 3: shipments.events llega primero en una creación -> crea la fila con la acción CREACION y sin actor aún
    @Test
    @DisplayName("shipments.events primero: crea la fila (CREACION) y el actor llega después")
    void shipmentEventFirstCreatesEntry() {
        // arrange (preparar)
        when(repository.findByCorrelationId(OPERACION)).thenReturn(Optional.empty());

        // act (actuar): previousStatus null = el envío recién se creó
        auditService.processShipmentEvent(envio("shp-1", null, "CREADO"));

        // assert (revisar)
        ArgumentCaptor<AuditEntry> captor = ArgumentCaptor.forClass(AuditEntry.class);
        verify(repository).save(captor.capture());
        AuditEntry fila = captor.getValue();
        assertEquals("CREACION", fila.getAction());
        assertNull(fila.getFromStatus());
        assertEquals("CREADO", fila.getToStatus());
        assertNull(fila.getActorId());
    }

    // test 4 (idempotencia): el mismo evento de audit.timeline llega dos veces -> la segunda no se guarda
    @Test
    @DisplayName("audit.timeline repetido: no se guarda dos veces")
    void repeatedTimelineIsIgnored() {
        // arrange (preparar): la fila ya tiene aplicado ese evento
        AuditEntry existente = AuditEntry.builder().correlationId(OPERACION).auditEventId("aud-1").build();
        when(repository.findByCorrelationId(OPERACION)).thenReturn(Optional.of(existente));

        // act (actuar)
        auditService.processAuditTimeline(timeline("aud-1", "CAMBIO_ESTADO", "EN_BODEGA", "EN_RUTA"));

        // assert (revisar)
        verify(repository, never()).save(any());
    }

    // test 5 (HU 9): el timeline de un envío sale como DTO, en el orden de la base (más antiguo primero)
    @Test
    @DisplayName("Timeline: las filas del envío salen como AuditEntryResponse")
    void timelineIsReturnedAsResponses() {
        // arrange (preparar)
        AuditEntry creado = AuditEntry.builder().shipmentId(42L).action("CREACION").toStatus("CREADO")
                .actorId("oid-cliente").eventTimestamp(CUANDO).build();
        AuditEntry aceptado = AuditEntry.builder().shipmentId(42L).action("CAMBIO_ESTADO").fromStatus("CREADO")
                .toStatus("ACEPTADO").actorId("oid-operador").eventTimestamp(CUANDO.plusSeconds(60)).build();
        when(repository.findByShipmentIdOrderByEventTimestampAsc(42L)).thenReturn(List.of(creado, aceptado));

        // act (actuar)
        List<AuditEntryResponse> timeline = auditService.getTimeline(42L);

        // assert (revisar)
        assertEquals(2, timeline.size());
        assertEquals("CREADO", timeline.get(0).toStatus());
        assertEquals("oid-operador", timeline.get(1).actorId());
    }
}
