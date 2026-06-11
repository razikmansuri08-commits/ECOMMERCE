package com.rmtech.ecom.Repositories;

import com.rmtech.ecom.Entities.Orders;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface Order_Repo extends JpaRepository<Orders,Long>, JpaSpecificationExecutor<Orders>
{
    @Query("SELECT o FROM Orders o WHERE o.user.Id=:userId")
    List<Orders> findByuserId(Long userId);

    Page<Orders> findAll(Pageable pageable);

    @Query("SELECT o FROM Orders o WHERE o.orderId=:orderId")
    Orders findByOrderId(@Param("orderId") String orderId);

    @Modifying
    @Query("DELETE FROM Order_items oi WHERE oi.order.user.Id = :userId")
    void deleteOrderItemsByUserId(@Param("userId") Long userId);

    @Query("SELECT o FROM Orders o WHERE o.user.Id = :userId")
    Page<Orders> findAllUserOrders(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Orders o WHERE o.user.Id=:userId")
    void deleteOrdersByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Order_items oi WHERE oi.order.orderId = :orderId")
    void deleteOrderItemsByOrderId(String orderId);
}
