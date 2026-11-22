package ruta_express.ms_audit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ruta_express.ms_audit.model.AuditEntry;

@Repository
public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {

    // findByCorrelationId (buscar por operación): la fila que ya creó el otro evento, si existe
    Optional<AuditEntry> findByCorrelationId(String correlationId);

    // timeline de un envío, del más antiguo al más nuevo
    List<AuditEntry> findByShipmentIdOrderByEventTimestampAsc(Long shipmentId);
}
