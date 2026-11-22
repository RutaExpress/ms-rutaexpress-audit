package ruta_express.ms_audit.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ruta_express.ms_audit.dto.AuditEntryResponse;
import ruta_express.ms_audit.service.AuditService;

// endpoint de solo lectura de audit (caso, sección 5); lo llama el BFF con rol AUDITOR o ADMIN
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    // GET /api/v1/audit/shipments/{id}: timeline del envío, del más antiguo al más nuevo
    @GetMapping("/shipments/{id}")
    public List<AuditEntryResponse> getTimeline(@PathVariable Long id) {
        return auditService.getTimeline(id);
    }
}
