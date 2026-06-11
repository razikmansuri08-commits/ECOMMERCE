package com.rmtech.ecom.unittests;

import com.rmtech.ecom.DTOS.*;
import com.rmtech.ecom.Entities.*;
import com.rmtech.ecom.Exception.InvalidOrderStatusException;
import com.rmtech.ecom.Exception.OrderNotFoundException;
import com.rmtech.ecom.Exception.UserNotFoundException;
import com.rmtech.ecom.Repositories.*;
import com.rmtech.ecom.Service.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
 class OrderServiceTest
{
 @Mock
 private Order_Repo order_Repo;
 @InjectMocks
 private Order_Service orderService;
 @Mock
 private User_Repo user_Repo;
 @Mock
 private Cart_Service cart_Service;
 @Mock
 private Inventory_Service inventory_Service;


 @Test
 void shouldPlaceOrder()
 {
  Product product = new Product();
  product.setId(1L);
  product.setName("Product");

  Cart_Items item = new Cart_Items();
  item.setProduct(product);
  item.setQuantity(2);

  Cart cart = new Cart();
  cart.setCart_items(List.of(item));

  User user = new User();
  user.setCart(cart);

  when(user_Repo.findbyusername("razik"))
          .thenReturn(user);

  when(order_Repo.save(any(Orders.class)))
          .thenAnswer(inv -> inv.getArgument(0));
  Order_Dto orderDto=orderService.place_ord("razik");

  ArgumentCaptor<Orders> captor =
          ArgumentCaptor.forClass(Orders.class);
  verify(order_Repo, times(1)).save(captor.capture());
  Orders order = captor.getValue();

  assertEquals("Product", orderDto.getItems().get(0).getProduct());
  verify(order_Repo, times(1)).save(any(Orders.class));
  verify(cart_Service, times(1)).clear_crt(any(String.class));
  assertEquals(OrderStatus.PENDING, order.getStatus());

  verify(inventory_Service, times(1)).decrease_stock(any(Long.class), any(Integer.class));
 }

 @Test
 void shouldThrowExceptionWhenUserNotFound() {
  when(user_Repo.findbyusername(anyString()))
          .thenReturn(null);
  assertThrows(UserNotFoundException.class, () -> orderService.place_ord("razik"));
 }

 @Test
 void shouldGetAllOrders() {
  Page<Orders>page=new PageImpl<>(List.of(new Orders(), new Orders()));
  when(order_Repo.findAll(any(Pageable.class)))
          .thenReturn(page);
  OrderPageResponse orderPageResponse=orderService.get_ords(PageRequest.of(0, 10));
  assertEquals(2,orderPageResponse.getTotalItems() );
  verify(order_Repo, times(1)).findAll(any(Pageable.class));
 }

 @Test
 void shouldReturnOrder()
 {
  User user = new User();
  user.setId(1L);
  user.setName("razik");
  when(user_Repo.findbyusername(anyString()))
          .thenReturn(user);
  Orders order = new Orders();
  order.setOrderId("orderId");
  order.setUser(user);
  when(order_Repo.findByOrderId(anyString()))
          .thenReturn(order);
  Order_Dto orderDto = orderService.get_ord("orderId","razik");
  assertEquals(order.getOrderId(), orderDto.getId());
 }

 @Test
 void shouldThrowExceptionWhenOrderNotFound() {
  User user = new User();
  user.setId(1L);
  user.setName("razik");
  when(user_Repo.findbyusername(anyString()))
          .thenReturn(user);
  when(order_Repo.findByOrderId(anyString()))
          .thenReturn(null);
  assertThrows(OrderNotFoundException.class, () -> orderService.get_ord("orderId","razik"));
 }
 @Test
 void shouldCancelOrder()
 {
  User user = new User();
  user.setId(1L);

  Product product = new Product();
  product.setId(10L);

  Order_items item = new Order_items();
  item.setProduct(product);
  item.setQuantity(2);

  Orders order = new Orders();
  order.setOrderId("ORD123");
  order.setUser(user);
  order.setStatus(OrderStatus.PENDING);
  order.setOrder_items(new ArrayList<>(List.of(item)));

  when(user_Repo.findbyusername("razik"))
          .thenReturn(user);

  when(order_Repo.findByOrderId("ORD123"))
          .thenReturn(order);

  boolean result =
          orderService.cancel_ord(
                  "ORD123",
                  "razik"
          );

  assertTrue(result);

  assertEquals(OrderStatus.CANCELLED, order.getStatus());

  assertTrue(order.getOrder_items().isEmpty());

  verify(inventory_Service)
          .increase_stock(10L, 2);

  verify(order_Repo)
          .deleteOrderItemsByOrderId("ORD123");

  verify(order_Repo)
          .delete(order);
 }

 @Test
 void shouldThrowExceptionWhenOrderNotFoundForCancel() {
  User user = new User();
  user.setId(1L);
  user.setName("razik");
  when(user_Repo.findbyusername(anyString()))
          .thenReturn(user);
  when(order_Repo.findByOrderId(anyString()))
          .thenReturn(null);
  assertThrows(OrderNotFoundException.class, () -> orderService.cancel_ord("orderId", "razik"));
 }
@Test
 void shouldThrowExceptionWhenUserNotFoundForCancel() {
  when(user_Repo.findbyusername(anyString()))
          .thenReturn(null);
  assertThrows(UserNotFoundException.class, () -> orderService.cancel_ord("orderId", "razik"));
 }

 @Test
 void shouldReturnUserOrderStatus()
 {
  User user =new User();
  user.setId(1L);
  user.setName("razik");
  Orders order=new Orders();
  order.setOrderId("orderId");
  user.getOrder().add(order);
  order.setStatus(OrderStatus.PENDING);
  order.setUser(user);

  when(user_Repo.findbyusername(anyString()))
          .thenReturn(user);

  when(order_Repo.findByOrderId(anyString()))
          .thenReturn(order);

  OrderStatus status = orderService.getUserOrderStatus("orderId","razik");
  assertEquals(OrderStatus.PENDING, status);
 }

 @Test
 void shouldthrowAccessDeniedExceptionUserOrderStatus()
 {
  User user1 =new User();
  user1.setId(1L);
  user1.setName("razik");

  User user2 =new User();
  user2.setId(2L);
  user2.setName("razik");

  Orders order=new Orders();
  order.setOrderId("orderId");
  order.setStatus(OrderStatus.PENDING);



  user2.setOrder(List.of(order));

  order.setUser(user2);

  when(user_Repo.findbyusername(anyString()))
          .thenReturn(user1);

  when(order_Repo.findByOrderId(anyString()))
          .thenReturn(order);
assertThrows(AccessDeniedException.class, () -> orderService.getUserOrderStatus("orderId","razik"));
 }

 @Test
 void shouldThrowAccessDeniedWhenUserIsNotAdmin() {
  Authentication auth =
          new UsernamePasswordAuthenticationToken(
                  "user",
                  "password",
                  List.of(
                          new SimpleGrantedAuthority(
                                  "ROLE_USER"
                          )
                  )
          );

  SecurityContextHolder
          .getContext()
          .setAuthentication(auth);

  assertThrows(
          AccessDeniedException.class,
          () -> orderService.updateStatus(
                  "orderId",
                  OrderStatus.SHIPPED
          )
  );
 }
 @Test
 void shouldThrowInvalidOrderStatusExceptionOnUpdateStatus() {
  Authentication auth =
          new UsernamePasswordAuthenticationToken(
                  "admin",
                  "password",
                  List.of(
                          new SimpleGrantedAuthority(
                                  "ROLE_ADMIN"
                          )
                  )
          );

  SecurityContextHolder
          .getContext()
          .setAuthentication(auth);
  Orders order = new Orders();
  order.setOrderId("orderId");
  order.setStatus(OrderStatus.PENDING);
  when(order_Repo.findByOrderId("orderId"))
          .thenReturn(order);
  assertThrows(
          InvalidOrderStatusException.class,
          () -> orderService.updateStatus(
                  "orderId",
                  OrderStatus.PENDING
          )
  );
 }
 @Test
 void shouldThrowOrderNotFoundExceptionOnUpdateStatus() {
  Authentication auth =
          new UsernamePasswordAuthenticationToken(
                  "admin",
                  "password",
                  List.of(
                          new SimpleGrantedAuthority(
                                  "ROLE_ADMIN"
                          )
                  )
          );

  SecurityContextHolder
          .getContext()
          .setAuthentication(auth);
  when(order_Repo.findByOrderId("orderId"))
          .thenReturn(null);
  assertThrows(
          OrderNotFoundException.class,
          () -> orderService.updateStatus(
                  "orderId",
                  OrderStatus.SHIPPED
          )
  );
 }
 @Test
 void shouldThrowInvalidOrderStatusException_2_OnUpdateStatus() {
  Authentication auth =
          new UsernamePasswordAuthenticationToken(
                  "admin",
                  "password",
                  List.of(
                          new SimpleGrantedAuthority(
                                  "ROLE_ADMIN"
                          )
                  )
          );

  SecurityContextHolder
          .getContext()
          .setAuthentication(auth);
  Orders order=new Orders();
  order.setOrderId("orderId");
  order.setStatus(OrderStatus.DELIVERED);

  when(order_Repo.findByOrderId("orderId"))
          .thenReturn(order);
  assertThrows(
          InvalidOrderStatusException.class,
          () -> orderService.updateStatus(
                  "orderId",
                  OrderStatus.PROCESSING
          )
  );
 }

 @Test
 void shouldSetShippedTimestampOnUpdateStatus()
 {
  Authentication auth =
          new UsernamePasswordAuthenticationToken(
                  "admin",
                  "password",
                  List.of(
                          new SimpleGrantedAuthority(
                                  "ROLE_ADMIN"
                          )
                  )
          );

  SecurityContextHolder
          .getContext()
          .setAuthentication(auth);
  Orders order = new Orders();
  order.setOrderId("orderId");
  order.setStatus(OrderStatus.PROCESSING);
  order.setShippedAt(LocalDateTime.now());

  when(order_Repo.findByOrderId("orderId"))
          .thenReturn(order);
  orderService.updateStatus("orderId", OrderStatus.SHIPPED);

  assertEquals(
          OrderStatus.SHIPPED,
          order.getStatus()
  );

  assertNotNull(
          order.getShippedAt()
  );
 }

 @Test
 void shouldUpdateStatusToCancelled()
 {
  Authentication auth =
          new UsernamePasswordAuthenticationToken(
                  "admin",
                  "password",
                  List.of(
                          new SimpleGrantedAuthority(
                                  "ROLE_ADMIN"
                          )
                  )
          );

  SecurityContextHolder
          .getContext()
          .setAuthentication(auth);

  Product product = new Product();
  product.setId(1L);

  Order_items order_items = new Order_items();
  order_items.setProduct(product);
  order_items.setQuantity(1);





  Orders order = new Orders();
  order.setOrderId("orderId");
  order.setStatus(OrderStatus.CONFIRMED);
  order.setShippedAt(LocalDateTime.now());

  order.setOrder_items(new ArrayList<>(List.of(order_items)));
order_items.setOrder(order);
  when(order_Repo.findByOrderId("orderId"))
          .thenReturn(order);

  orderService.updateStatus("orderId", OrderStatus.CANCELLED);

  assertEquals(
          OrderStatus.CANCELLED,
          order.getStatus()
  );
  verify(inventory_Service, times(1))
          .addstock(
                  anyLong(),
                  anyInt()
           );

 }

 }
