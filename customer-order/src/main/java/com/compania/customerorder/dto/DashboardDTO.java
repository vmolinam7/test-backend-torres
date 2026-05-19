package com.compania.customerorder.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {

    private long totalPedidos;
    private long pedidosCompletados;
    private long pedidosPendientes;
    private long pedidosCancelados;
    private long clientesActivos;
    private long totalClientes;

    private List<Map<String, Object>> actividadPorDia;
    private List<Map<String, Object>> actividadPorMes;
}
