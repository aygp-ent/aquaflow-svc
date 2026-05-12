package com.mumsaqua.repository;

import com.mumsaqua.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCustomerIdOrderByPaymentDateDesc(Long customerId);
    List<Payment> findBySaleId(Long saleId);
    List<Payment> findAllByOrderByPaymentDateDesc();
}
