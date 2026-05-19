package com.compania.customerorder.service;

import com.compania.customerorder.dto.OrderDTO;
import com.compania.customerorder.entity.Customer;
import com.compania.customerorder.entity.Order;
import com.compania.customerorder.entity.OrderStatus;
import com.compania.customerorder.repository.CustomerRepository;
import com.compania.customerorder.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
        return toDTO(order);
    }

    public List<OrderDTO> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByStatus(String estado) {
        OrderStatus status = OrderStatus.valueOf(estado.toUpperCase());
        return orderRepository.findByEstado(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByFechaPedidoBetween(start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> filterOrders(Long customerId, String estado, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<Order> orders;

        if (customerId != null && estado != null) {
            orders = orderRepository.findByCustomerIdAndEstado(customerId, OrderStatus.valueOf(estado.toUpperCase()));
        } else if (customerId != null) {
            orders = orderRepository.findByCustomerId(customerId);
        } else if (estado != null) {
            orders = orderRepository.findByEstado(OrderStatus.valueOf(estado.toUpperCase()));
        } else if (fechaInicio != null && fechaFin != null) {
            orders = orderRepository.findByFechaPedidoBetween(fechaInicio, fechaFin);
        } else {
            orders = orderRepository.findAll();
        }

        return orders.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public OrderDTO createOrder(OrderDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + dto.getCustomerId()));

        Order order = Order.builder()
                .descripcion(dto.getDescripcion())
                .cantidad(dto.getCantidad())
                .precioUnitario(dto.getPrecioUnitario())
                .customer(customer)
                .estado(OrderStatus.PENDIENTE)
                .build();

        order.calculateTotal();

        Order saved = orderRepository.save(order);
        return toDTO(saved);
    }

    public OrderDTO updateOrder(Long id, OrderDTO dto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));

        order.setDescripcion(dto.getDescripcion());
        order.setCantidad(dto.getCantidad());
        order.setPrecioUnitario(dto.getPrecioUnitario());
        order.calculateTotal();

        if (dto.getCustomerId() != null && !dto.getCustomerId().equals(order.getCustomer().getId())) {
            Customer newCustomer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + dto.getCustomerId()));
            order.setCustomer(newCustomer);
        }

        Order updated = orderRepository.save(order);
        return toDTO(updated);
    }

    public OrderDTO updateOrderStatus(Long id, String estado) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));

        OrderStatus newStatus = OrderStatus.valueOf(estado.toUpperCase());
        order.setEstado(newStatus);

        Order updated = orderRepository.save(order);
        return toDTO(updated);
    }

    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Pedido no encontrado con ID: " + id);
        }
        orderRepository.deleteById(id);
    }

    private OrderDTO toDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .descripcion(order.getDescripcion())
                .cantidad(order.getCantidad())
                .precioUnitario(order.getPrecioUnitario())
                .total(order.getTotal())
                .estado(order.getEstado().name())
                .fechaPedido(order.getFechaPedido() != null ? order.getFechaPedido().toString() : null)
                .fechaActualizacion(order.getFechaActualizacion() != null ? order.getFechaActualizacion().toString() : null)
                .customerId(order.getCustomer().getId())
                .customerNombre(order.getCustomer().getNombre() + " " + order.getCustomer().getApellido())
                .build();
    }
}
