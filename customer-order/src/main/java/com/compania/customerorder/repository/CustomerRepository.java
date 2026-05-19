package com.compania.customerorder.repository;

import com.compania.customerorder.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByActivoTrue();

    boolean existsByEmail(String email);

    long countByActivoTrue();
}
