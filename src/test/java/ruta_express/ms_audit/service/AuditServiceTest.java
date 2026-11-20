package ruta_express.ms_audit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ruta_express.ms_audit.dto.AuditEventDTO;
import ruta_express.ms_audit.model.AuditEntry;
import ruta_express.ms_audit.repository.AuditEntryRepository;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditEntryRepository repository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void processAuditTimeline_DebeGuardarEnBaseDeDatos() {
        // Arrange
        AuditEventDTO.Payload payload = new AuditEventDTO.Payload(
                42L, "RX-123", "user-1", "CAMBIO_ESTADO", "CREADO", "ACEPTADO", "ms-shipments"
        );
        AuditEventDTO evento = new AuditEventDTO(
                "evt-001", "AUDIT_STATUS_CHANGED", Instant.now(), "trace-1", "corr-1", payload
        );

        // Act
        auditService.processAuditTimeline(evento);

        // Assert
        verify(repository, times(1)).save(any(AuditEntry.class));
    }
}