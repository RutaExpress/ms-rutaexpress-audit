package ruta_express.ms_audit.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ruta_express.ms_audit.dto.AuditEntryResponse;
import ruta_express.ms_audit.service.AuditService;

// test de la capa web de audit: el timeline de un envío y un id inválido
@WebMvcTest(AuditController.class)
class AuditControllerTest {

    // simulador de peticiones HTTP
    @Autowired
    private MockMvc mockMvc;

    // dependencia simulada (servicio de auditoría)
    @MockitoBean
    private AuditService auditService;

    // test 1 (HU 9, caso sección 5): GET /shipments/{id} devuelve el timeline del envío
    @Test
    @DisplayName("GET /shipments/42: timeline con quién, qué y cuándo")
    void timelineOfShipment() throws Exception {
        // arrange (preparar)
        AuditEntryResponse paso = new AuditEntryResponse("corr-1", 42L, "RX-20261121-ABC123", "CAMBIO_ESTADO",
                "CREADO", "ACEPTADO", "oid-operador", "ms-rutaexpress-shipments", "oid-cliente", "svc-1",
                Instant.parse("2026-11-21T10:05:00Z"));
        when(auditService.getTimeline(42L)).thenReturn(List.of(paso));

        // act + assert (actuar y revisar)
        mockMvc.perform(get("/api/v1/audit/shipments/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].actorId").value("oid-operador"))
                .andExpect(jsonPath("$[0].toStatus").value("ACEPTADO"))
                .andExpect(jsonPath("$[0].action").value("CAMBIO_ESTADO"));
    }

    // test 2 (caso de error, contrato 5): un id que no es número -> 400 con ProblemDetail (no 500)
    @Test
    @DisplayName("GET /shipments/abc: 400")
    void nonNumericIdGets400() throws Exception {
        mockMvc.perform(get("/api/v1/audit/shipments/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Solicitud inválida"));
    }
}
