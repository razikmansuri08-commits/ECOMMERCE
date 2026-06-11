package com.rmtech.ecom.Service;

import com.rmtech.ecom.DTOS.OrderPageResponse;
import com.rmtech.ecom.DTOS.Order_Dto;
import com.rmtech.ecom.DTOS.Order_ItemsDto;
import com.rmtech.ecom.DTOS.User_dto;
import com.rmtech.ecom.Entities.*;
import com.rmtech.ecom.Exception.InvalidOrderStatusException;
import com.rmtech.ecom.Exception.OrderNotFoundException;
import com.rmtech.ecom.Exception.UserNotFoundException;
import com.rmtech.ecom.Repositories.Inventory_Repo;
import com.rmtech.ecom.Repositories.Order_Repo;
import com.rmtech.ecom.Repositories.Product_Repo;
import com.rmtech.ecom.Repositories.User_Repo;
import jakarta.transaction.Transactional;
import lombok.experimental.Helper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class Order_Service {
    private final Order_Repo or;
    private final User_Repo ur;
    private final Cart_Service cs;
    private final Inventory_Service is;

    public Order_Service(Order_Repo or, User_Repo ur, Cart_Service cs, Inventory_Service is) {
        this.or = or;
        this.ur = ur;
        this.cs = cs;
        this.is = is;
    }
    private static final Map<OrderStatus,
            Set<OrderStatus>> ALLOWED_TRANSITIONS =
            Map.of(
                    OrderStatus.PENDING,
                    Set.of(
                            OrderStatus.CONFIRMED,
                            OrderStatus.CANCELLED
                    ),
                    OrderStatus.DELIVERED,
                    Set.of(),
                    OrderStatus.CANCELLED,
                    Set.of(),

                    OrderStatus.CONFIRMED,
                    Set.of(
                            OrderStatus.PROCESSING,
                            OrderStatus.CANCELLED
                    ),

                    OrderStatus.PROCESSING,
                    Set.of(
                            OrderStatus.SHIPPED
                    ),

                    OrderStatus.SHIPPED,
                    Set.of(
                            OrderStatus.DELIVERED
                    )
            );


    @Transactional
    public Order_Dto place_ord(String username)
    {
        User user = ur.findbyusername(username);
        if (user == null)
            throw new UserNotFoundException("User not found");
        Cart cart = user.getCart();
        if (cart == null) {
            throw new IllegalArgumentException("cart not found");
        }
        if (cart.getCart_items().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Orders order = new Orders();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderedAt(LocalDateTime.now());

        List<Order_items> orderItems = new ArrayList<>();
        for (Cart_Items ci : cart.getCart_items()) {

            is.decrease_stock(ci.getProduct().getId(),ci.getQuantity());

            Order_items oi = new Order_items();
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setOrder(order);
            orderItems.add(oi);
        }

        order.setOrder_items(orderItems);
        or.save(order);
        cs.clear_crt(username);
        return convertToDTO(order);
    }
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public OrderStatus updateStatus(
            String orderId,
            OrderStatus status)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Access denied");
        }
        Orders order = or.findByOrderId(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found");
        }

        OrderStatus previousStatus = order.getStatus();
        if (previousStatus == status) {
            throw new InvalidOrderStatusException(
                    "Order is already in status: " + status);
        }
        validateTransition(order.getStatus(), status);
        if (status == OrderStatus.SHIPPED) {
            order.setShippedAt(LocalDateTime.now());
        }
        if (status == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        if (status == OrderStatus.CANCELLED) {
            order.setCancelledAt(LocalDateTime.now());
            order.getOrder_items()
                    .forEach(item ->
                            is.addstock(
                                    item.getProduct().getId(),
                                    item.getQuantity()
                            ));
        }

         order.setStatus(status);
        return status;
    }

@PreAuthorize("hasRole('ADMIN')")
    public OrderPageResponse get_ords(Pageable pageable) {

        Page<Orders> ordersPage=or.findAll(pageable);
        List<Order_Dto> orderDtos=ordersPage.getContent().stream().map(this::convertToDTO).toList();
        OrderPageResponse orderPageResponse=new OrderPageResponse();
        orderPageResponse.setOrders(orderDtos);
        orderPageResponse.setCurrentPage(ordersPage.getNumber());
        orderPageResponse.setTotalPages(ordersPage.getTotalPages());
        orderPageResponse.setTotalItems(ordersPage.getTotalElements());
        return orderPageResponse;
    }

    public OrderPageResponse get_UserOrds(Pageable pageable) {
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user=ur.findbyusername(username);
        if (user == null)
            throw new UserNotFoundException("User not found");


        Page<Orders> ordersPage=or.findAllUserOrders(user.getId(), pageable);
        List<Order_Dto> orderDtos=ordersPage.getContent().stream().map(this::convertToDTO).toList();
        OrderPageResponse orderPageResponse=new OrderPageResponse();
        orderPageResponse.setOrders(orderDtos);
        orderPageResponse.setCurrentPage(ordersPage.getNumber());
        orderPageResponse.setTotalPages(ordersPage.getTotalPages());
        orderPageResponse.setTotalItems(ordersPage.getTotalElements());
        return orderPageResponse;
    }



    @Transactional
    public Order_Dto get_ord(String orderId, String username) {
        User user = ur.findbyusername(username);
        if (user == null)
            throw new UserNotFoundException("User not found");
        Orders order=or.findByOrderId(orderId);
        if(order==null||order.getUser()==null)
            throw new OrderNotFoundException("order not found");
        else if (order.getUser().getId().equals(user.getId()))
        {
            return convertToDTO(order);
        }
        else
        {
            throw new OrderNotFoundException("Incorrect order Id");
        }
    }

    public OrderStatus getUserOrderStatus(String id,String username)
    {
        User user = ur.findbyusername(username);
        if (user == null)
            throw new UserNotFoundException("User not found");
        Orders order=or.findByOrderId(id);
        if(order==null)
            throw new OrderNotFoundException("order not found");
        if(!order.getUser().getId().equals(user.getId()))
            throw new AccessDeniedException(
"Access Denied: You do not have permission to access this order.");
        return order.getStatus();
    }

@PreAuthorize("hasRole('ADMIN')")
    public OrderStatus getOrderStatus(String id) {
        Orders order = or.findByOrderId(id);
        if(order==null||order.getUser()==null)
            throw new OrderNotFoundException("order not found");
        return order.getStatus();
    }
    @Transactional
    public boolean cancel_ord(String orderId, String username)
    {
        User user = ur.findbyusername(username);
        if (user == null)
            throw new UserNotFoundException("User not found");
        Orders order = or.findByOrderId(orderId);
        if(order==null||order.getUser()==null)
            throw new OrderNotFoundException("order not found");

        if (order.getUser().getId().equals(user.getId()))
        {
            validateTransition(order.getStatus(), OrderStatus.CANCELLED);
            order.getOrder_items().forEach(oi->is.increase_stock(oi.getProduct().getId(),oi.getQuantity()));
            order.getOrder_items().clear();
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(LocalDateTime.now());

            or.deleteOrderItemsByOrderId(orderId);
            or.delete(order);
            return true;
        }
        else
        {
            throw new OrderNotFoundException("Incorrect order Id");
        }
    }

    public void delete_all_ords(String username)
    {
        User user = ur.findbyusername(username);
        if (user == null)
            throw new UserNotFoundException("User not found");
        or.deleteOrderItemsByUserId(user.getId());
        or.deleteOrdersByUserId(user.getId());
    }


    private Order_Dto convertToDTO(Orders order) {
        Order_Dto orderDto = new Order_Dto();
        orderDto.setId((order.getOrderId()));
        List<Order_ItemsDto> itemsDto = order.getOrder_items().stream().map(orderItems ->
        {
            Order_ItemsDto orderItemsDto = new Order_ItemsDto();
            orderItemsDto.setProduct(orderItems.getProduct().getName());
            orderItemsDto.setQuantity(orderItems.getQuantity());
            orderItemsDto.setOrder_item_id(orderItems.getOrder_item_id());
            return orderItemsDto;
        }).toList();
        orderDto.setItems(itemsDto);
        return orderDto;
    }

    private void validateTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (!ALLOWED_TRANSITIONS
                .get(currentStatus)
                .contains(newStatus))
        {
            throw new InvalidOrderStatusException(
                    "Invalid status transition from "
                            + currentStatus +
                            " to " +
                            newStatus
            );
        }
    }


}

