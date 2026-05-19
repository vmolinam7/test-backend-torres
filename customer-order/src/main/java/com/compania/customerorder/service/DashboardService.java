package com.compania.customerorder.service;

import com.compania.customerorder.dto.DashboardDTO;
import com.compania.customerorder.entity.OrderStatus;
import com.compania.customerorder.repository.CustomerRepository;
import com.compania.customerorder.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public DashboardDTO getStats() {
        long totalPedidos = orderRepository.count();
        long completados = orderRepository.countByEstado(OrderStatus.COMPLETADO);
        long pendientes = orderRepository.countByEstado(OrderStatus.PENDIENTE);
        long cancelados = orderRepository.countByEstado(OrderStatus.CANCELADO);
        long clientesActivos = customerRepository.countByActivoTrue();
        long totalClientes = customerRepository.count();

        return DashboardDTO.builder()
                .totalPedidos(totalPedidos)
                .pedidosCompletados(completados)
                .pedidosPendientes(pendientes)
                .pedidosCancelados(cancelados)
                .clientesActivos(clientesActivos)
                .totalClientes(totalClientes)
                .build();
    }

    public DashboardDTO getStatsWithActivity() {
        DashboardDTO stats = getStats();

        // Actividad últimos 30 días
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Object[]> dailyData = orderRepository.countOrdersPerDay(thirtyDaysAgo);
        List<Map<String, Object>> actividadDia = new ArrayList<>();
        for (Object[] row : dailyData) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("fecha", row[0] != null ? row[0].toString() : "");
            entry.put("total", row[1]);
            actividadDia.add(entry);
        }

        // Actividad últimos 12 meses
        LocalDateTime twelveMonthsAgo = LocalDateTime.now().minusMonths(12);
        List<Object[]> monthlyData = orderRepository.countOrdersPerMonth(twelveMonthsAgo);
        List<Map<String, Object>> actividadMes = new ArrayList<>();
        for (Object[] row : monthlyData) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("anio", row[0]);
            entry.put("mes", row[1]);
            entry.put("total", row[2]);
            actividadMes.add(entry);
        }

        stats.setActividadPorDia(actividadDia);
        stats.setActividadPorMes(actividadMes);

        return stats;
    }
}
