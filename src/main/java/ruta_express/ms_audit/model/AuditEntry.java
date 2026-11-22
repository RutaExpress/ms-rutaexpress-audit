package ruta_express.ms_audit.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// una fila por OPERACIÓN de shipments (creación o cambio de estado), armada con sus 2 eventos de Kafka
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_entry")
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // la operación: los 2 eventos traen el mismo correlationId; único para no duplicar la fila
    @Column(unique = true, nullable = false)
    private String correlationId;
    private String traceId;

    // eventId de cada lado: si ya está lleno, ese evento ya se aplicó (idempotencia)
    @Column(unique = true)
    private String auditEventId;      // de audit.timeline
    @Column(unique = true)
    private String shipmentEventId;   // de shipments.events

    // del envío (los traen los 2 eventos)
    private Long shipmentId;
    private String trackingNumber;
    private String fromStatus;
    private String toStatus;
    private Instant eventTimestamp;

    // de audit.timeline: quién, qué y desde dónde
    private String actorId;
    private String action;
    private String source;

    // de shipments.events: datos del envío
    private String customerId;
    private String serviceId;
}
