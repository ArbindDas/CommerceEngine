package com.JSR.order_service.repository;

import com.JSR.order_service.entites.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderRepository extends JpaRepository<Order , Long> {
}
