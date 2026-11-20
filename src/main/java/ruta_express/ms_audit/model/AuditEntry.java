package ruta_express.ms_audit.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

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

    @Column(unique = true)
    private String eventId;
    private Long shipmentId;
    private String trackingNumber;
    private String actorId;
    private String action;
    private String fromStatus;
    private String toStatus;
    private String source;
    private Instant eventTimestamp;
}