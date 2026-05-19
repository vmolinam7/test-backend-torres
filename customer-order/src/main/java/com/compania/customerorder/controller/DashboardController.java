package com.compania.customerorder.controller;

import com.compania.customerorder.dto.DashboardDTO;
import com.compania.customerorder.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Estadísticas y gráficas de actividad")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Estadísticas generales", description = "Retorna totales de pedidos, estados y clientes activos")
    @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    public ResponseEntity<DashboardDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/activity")
    @Operation(summary = "Actividad por período", description = "Retorna estadísticas con actividad por día (últimos 30 días) y por mes (últimos 12 meses)")
    @ApiResponse(responseCode = "200", description = "Datos de actividad obtenidos exitosamente")
    public ResponseEntity<DashboardDTO> getActivity() {
        return ResponseEntity.ok(dashboardService.getStatsWithActivity());
    }
}
