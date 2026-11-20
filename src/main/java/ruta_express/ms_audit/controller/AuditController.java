package ruta_express.ms_audit.controller;

import ruta_express.ms_audit.model.AuditEntry;
import ruta_express.ms_audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    // GET /api/v1/audit/shipments/{id}
    @GetMapping("/shipments/{id}")
    public List<AuditEntry> getTimeline(@PathVariable Long id) {
        return auditService.getTimeline(id);
    }
}