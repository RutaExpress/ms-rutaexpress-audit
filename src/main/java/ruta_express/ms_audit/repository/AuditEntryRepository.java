package ruta_express.ms_audit.repository;

import ruta_express.ms_audit.model.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {
    
    // Busca todo el historial de un envío específico, ordenado por fecha
    List<AuditEntry> findByShipmentIdOrderByEventTimestampAsc(Long shipmentId);
}