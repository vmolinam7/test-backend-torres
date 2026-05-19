package com.compania.customerorder.controller;

import com.compania.customerorder.dto.OrderDTO;
import com.compania.customerorder.security.JwtAuthenticationFilter;
import com.compania.customerorder.security.JwtService;
import com.compania.customerorder.security.SecurityConfig;
import com.compania.customerorder.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        orderDTO = OrderDTO.builder()
                .id(1L)
                .descripcion("Pedido de prueba")
                .cantidad(5)
                .precioUnitario(new BigDecimal("100.00"))
                .total(new BigDecimal("500.00"))
                .estado("PENDIENTE")
                .customerId(1L)
                .customerNombre("Juan Pérez")
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/orders - Listar pedidos")
    void getAllOrders_Returns200() throws Exception {
        when(orderService.filterOrders(any(), any(), any(), any())).thenReturn(Arrays.asList(orderDTO));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descripcion").value("Pedido de prueba"))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/orders?estado=PENDIENTE - Filtrar por estado")
    void getOrdersByStatus_Returns200() throws Exception {
        when(orderService.filterOrders(isNull(), eq("PENDIENTE"), isNull(), isNull()))
                .thenReturn(Arrays.asList(orderDTO));

        mockMvc.perform(get("/api/orders").param("estado", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/orders/{id} - Obtener pedido por ID")
    void getOrderById_Returns200() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(orderDTO);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.total").value(500.00));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/orders - Crear pedido")
    void createOrder_Returns201() throws Exception {
        when(orderService.createOrder(any(OrderDTO.class))).thenReturn(orderDTO);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descripcion").value("Pedido de prueba"));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/orders/{id} - Actualizar pedido")
    void updateOrder_Returns200() throws Exception {
        when(orderService.updateOrder(eq(1L), any(OrderDTO.class))).thenReturn(orderDTO);

        mockMvc.perform(put("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Pedido de prueba"));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/orders/{id}/status - Cambiar estado")
    void updateOrderStatus_Returns200() throws Exception {
        OrderDTO completedOrder = OrderDTO.builder()
                .id(1L)
                .descripcion("Pedido de prueba")
                .estado("COMPLETADO")
                .customerId(1L)
                .customerNombre("Juan Pérez")
                .build();

        when(orderService.updateOrderStatus(1L, "COMPLETADO")).thenReturn(completedOrder);

        mockMvc.perform(patch("/api/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "COMPLETADO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/orders/{id} - Eliminar pedido")
    void deleteOrder_Returns204() throws Exception {
        doNothing().when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/orders sin autenticación retorna 403")
    void createOrder_NoAuth_Returns403() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isForbidden());
    }
}
