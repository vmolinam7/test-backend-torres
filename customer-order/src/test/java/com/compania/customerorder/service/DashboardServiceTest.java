package com.compania.customerorder.service;

import com.compania.customerorder.dto.DashboardDTO;
import com.compania.customerorder.entity.OrderStatus;
import com.compania.customerorder.repository.CustomerRepository;
import com.compania.customerorder.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Obtener estadísticas generales")
    void getStats_ReturnsCorrectCounts() {
        when(orderRepository.count()).thenReturn(50L);
        when(orderRepository.countByEstado(OrderStatus.COMPLETADO)).thenReturn(20L);
        when(orderRepository.countByEstado(OrderStatus.PENDIENTE)).thenReturn(25L);
        when(orderRepository.countByEstado(OrderStatus.CANCELADO)).thenReturn(5L);
        when(customerRepository.countByActivoTrue()).thenReturn(30L);
        when(customerRepository.count()).thenReturn(35L);

        DashboardDTO result = dashboardService.getStats();

        assertNotNull(result);
        assertEquals(50L, result.getTotalPedidos());
        assertEquals(20L, result.getPedidosCompletados());
        assertEquals(25L, result.getPedidosPendientes());
        assertEquals(5L, result.getPedidosCancelados());
        assertEquals(30L, result.getClientesActivos());
        assertEquals(35L, result.getTotalClientes());
    }

    @Test
    @DisplayName("Obtener estadísticas con actividad incluye datos por día y mes")
    void getStatsWithActivity_ReturnsActivityData() {
        when(orderRepository.count()).thenReturn(10L);
        when(orderRepository.countByEstado(OrderStatus.COMPLETADO)).thenReturn(5L);
        when(orderRepository.countByEstado(OrderStatus.PENDIENTE)).thenReturn(3L);
        when(orderRepository.countByEstado(OrderStatus.CANCELADO)).thenReturn(2L);
        when(customerRepository.countByActivoTrue()).thenReturn(8L);
        when(customerRepository.count()).thenReturn(10L);
        when(orderRepository.countOrdersPerDay(any())).thenReturn(new ArrayList<>());
        when(orderRepository.countOrdersPerMonth(any())).thenReturn(new ArrayList<>());

        DashboardDTO result = dashboardService.getStatsWithActivity();

        assertNotNull(result);
        assertEquals(10L, result.getTotalPedidos());
        assertNotNull(result.getActividadPorDia());
        assertNotNull(result.getActividadPorMes());
    }

    @Test
    @DisplayName("Dashboard con cero pedidos retorna conteos en cero")
    void getStats_NoPedidos_ReturnsZeros() {
        when(orderRepository.count()).thenReturn(0L);
        when(orderRepository.countByEstado(OrderStatus.COMPLETADO)).thenReturn(0L);
        when(orderRepository.countByEstado(OrderStatus.PENDIENTE)).thenReturn(0L);
        when(orderRepository.countByEstado(OrderStatus.CANCELADO)).thenReturn(0L);
        when(customerRepository.countByActivoTrue()).thenReturn(0L);
        when(customerRepository.count()).thenReturn(0L);

        DashboardDTO result = dashboardService.getStats();

        assertEquals(0L, result.getTotalPedidos());
        assertEquals(0L, result.getPedidosCompletados());
        assertEquals(0L, result.getClientesActivos());
    }
}
