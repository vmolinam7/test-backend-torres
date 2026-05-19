package com.compania.customerorder.repository;

import com.compania.customerorder.entity.Order;
import com.compania.customerorder.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByEstado(OrderStatus estado);

    List<Order> findByFechaPedidoBetween(LocalDateTime start, LocalDateTime end);

    List<Order> findByCustomerIdAndEstado(Long customerId, OrderStatus estado);

    long countByEstado(OrderStatus estado);

    @Query("SELECT cast(o.fechaPedido as date) as fecha, COUNT(o) as total " +
            "FROM Order o WHERE o.fechaPedido >= :startDate " +
            "GROUP BY cast(o.fechaPedido as date) " +
            "ORDER BY cast(o.fechaPedido as date)")
    List<Object[]> countOrdersPerDay(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT YEAR(o.fechaPedido), MONTH(o.fechaPedido), COUNT(o) " +
            "FROM Order o WHERE o.fechaPedido >= :startDate " +
            "GROUP BY YEAR(o.fechaPedido), MONTH(o.fechaPedido) " +
            "ORDER BY YEAR(o.fechaPedido), MONTH(o.fechaPedido)")
    List<Object[]> countOrdersPerMonth(@Param("startDate") LocalDateTime startDate);
}
