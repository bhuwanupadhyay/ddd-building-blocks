package io.github.bhuwanupadhyay.rtms.order.v1;

import com.google.common.flogger.FluentLogger;
import io.github.bhuwanupadhyay.rtms.order.domain.Order;
import io.github.bhuwanupadhyay.rtms.orders.v1.CreateOrder;
import io.github.bhuwanupadhyay.rtms.orders.v1.OrderPageList;
import io.github.bhuwanupadhyay.rtms.orders.v1.OrderResource;
import io.github.bhuwanupadhyay.rtms.orders.v1.OrdersApi;
import java.util.Optional;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
class WebOrderApi implements OrdersApi {

  private static final FluentLogger LOG = FluentLogger.forEnclosingClass();

  private final AppService appService;
  private final OrderQueryRepository queryRepository;

  WebOrderApi(AppService appService, OrderQueryRepository queryRepository) {
    this.appService = appService;
    this.queryRepository = queryRepository;
  }

  @Override
  public Mono<ResponseEntity<OrderPageList>> getOrders(
      String filterJson, String sort, ServerWebExchange exchange) {
    return null;
  }

  @Override
  public Mono<ResponseEntity<OrderResource>> getOrdersByOrderId(
      String orderId, ServerWebExchange exchange) {
    return null;
  }

  @Override
  public Mono<ResponseEntity<OrderResource>> postOrders(
      Mono<CreateOrder> createOrder, ServerWebExchange exchange) {
    return createOrder
        .doFirst(() -> LOG.atInfo().log("Creating new order."))
        .map(appService::placeOrder)
        .map(order -> ResponseEntity.ok(toOrderResource(order)))
        .doOnError(throwable -> LOG.atSevere().log("Unable to create new order."))
        .doOnSuccess(
            response ->
                LOG.atInfo().log(
                    "Order placed with order id %s",
                    Optional.ofNullable(response)
                        .map(HttpEntity::getBody)
                        .map(OrderResource::getId)
                        .orElseThrow(AppWebException::new)));
  }

  private OrderResource toOrderResource(Order order) {
    return new OrderResource()
        .id(order.getId().getReference())
        .contactPhone(order.getContactPhone())
        .customerId(order.getCustomerId())
        .productId(order.getProductId())
        .deliveryAddress(order.getDeliveryAddress());
  }
}
