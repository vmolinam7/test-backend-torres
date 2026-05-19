package com.compania.customerorder.service;

import com.compania.customerorder.dto.OrderDTO;
import com.compania.customerorder.entity.Customer;
import com.compania.customerorder.entity.Order;
import com.compania.customerorder.entity.OrderStatus;
import com.compania.customerorder.repository.CustomerRepository;
import com.compania.customerorder.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private Order order;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@test.com")
                .activo(true)
                .orders(new ArrayList<>())
                .build();

        order = Order.builder()
                .id(1L)
                .descripcion("Pedido de prueba")
                .cantidad(5)
                .precioUnitario(new BigDecimal("100.00"))
                .total(new BigDecimal("500.00"))
                .estado(OrderStatus.PENDIENTE)
                .fechaPedido(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .customer(customer)
                .build();

        orderDTO = OrderDTO.builder()
                .descripcion("Pedido de prueba")
                .cantidad(5)
                .precioUnitario(new BigDecimal("100.00"))
                .customerId(1L)
                .build();
    }

    @Test
    @DisplayName("Listar todos los pedidos")
    void getAllOrders_ReturnsList() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order));

        List<OrderDTO> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pedido de prueba", result.get(0).getDescripcion());
    }

    @Test
    @DisplayName("Obtener pedido por ID")
    void getOrderById_Exists_ReturnsOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderDTO result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals("Pedido de prueba", result.getDescripcion());
        assertEquals("PENDIENTE", result.getEstado());
    }

    @Test
    @DisplayName("Obtener pedido inexistente lanza excepción")
    void getOrderById_NotFound_ThrowsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.getOrderById(99L));
    }

    @Test
    @DisplayName("Crear pedido exitosamente")
    void createOrder_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDTO result = orderService.createOrder(orderDTO);

        assertNotNull(result);
        assertEquals("Pedido de prueba", result.getDescripcion());
        assertEquals(1L, result.getCustomerId());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Crear pedido con cliente inexistente lanza excepción")
    void createOrder_CustomerNotFound_ThrowsException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        orderDTO.setCustomerId(99L);

        assertThrows(RuntimeException.class, () -> orderService.createOrder(orderDTO));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Actualizar pedido exitosamente")
    void updateOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderDTO.setDescripcion("Pedido actualizado");
        orderDTO.setCantidad(10);
        OrderDTO result = orderService.updateOrder(1L, orderDTO);

        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Cambiar estado a COMPLETADO")
    void updateOrderStatus_ToCompleted() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        
        Order completedOrder = Order.builder()
                .id(1L)
                .descripcion("Pedido de prueba")
                .cantidad(5)
                .precioUnitario(new BigDecimal("100.00"))
                .total(new BigDecimal("500.00"))
                .estado(OrderStatus.COMPLETADO)
                .fechaPedido(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .customer(customer)
                .build();
        
        when(orderRepository.save(any(Order.class))).thenReturn(completedOrder);

        OrderDTO result = orderService.updateOrderStatus(1L, "COMPLETADO");

        assertNotNull(result);
        assertEquals("COMPLETADO", result.getEstado());
    }

    @Test
    @DisplayName("Cambiar estado a CANCELADO")
    void updateOrderStatus_ToCancelled() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        
        Order cancelledOrder = Order.builder()
                .id(1L)
                .descripcion("Pedido de prueba")
                .cantidad(5)
                .precioUnitario(new BigDecimal("100.00"))
                .total(new BigDecimal("500.00"))
                .estado(OrderStatus.CANCELADO)
                .fechaPedido(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .customer(customer)
                .build();
        
        when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);

        OrderDTO result = orderService.updateOrderStatus(1L, "CANCELADO");

        assertNotNull(result);
        assertEquals("CANCELADO", result.getEstado());
    }

    @Test
    @DisplayName("Filtrar pedidos por estado")
    void getOrdersByStatus_ReturnsList() {
        when(orderRepository.findByEstado(OrderStatus.PENDIENTE)).thenReturn(Arrays.asList(order));

        List<OrderDTO> result = orderService.getOrdersByStatus("PENDIENTE");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PENDIENTE", result.get(0).getEstado());
    }

    @Test
    @DisplayName("Filtrar pedidos por cliente")
    void getOrdersByCustomer_ReturnsList() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(Arrays.asList(order));

        List<OrderDTO> result = orderService.getOrdersByCustomer(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCustomerId());
    }

    @Test
    @DisplayName("Eliminar pedido exitosamente")
    void deleteOrder_Success() {
        when(orderRepository.existsById(1L)).thenReturn(true);
        doNothing().when(orderRepository).deleteById(1L);

        assertDoesNotThrow(() -> orderService.deleteOrder(1L));
        verify(orderRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar pedido inexistente lanza excepción")
    void deleteOrder_NotFound_ThrowsException() {
        when(orderRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> orderService.deleteOrder(99L));
        verify(orderRepository, never()).deleteById(any());
    }
}
