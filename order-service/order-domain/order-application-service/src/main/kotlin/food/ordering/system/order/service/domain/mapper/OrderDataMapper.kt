package food.ordering.system.order.service.domain.mapper

import com.food.ordering.system.domain.valueobject.*
import com.food.ordering.system.order.service.domain.entity.Order
import com.food.ordering.system.order.service.domain.entity.OrderItem
import com.food.ordering.system.order.service.domain.entity.Product
import com.food.ordering.system.order.service.domain.entity.Restaurant
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress
import food.ordering.system.order.service.domain.dto.create.CreateOrderCommand
import food.ordering.system.order.service.domain.dto.create.CreateOrderResponse
import food.ordering.system.order.service.domain.dto.create.OrderAddress
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OrderDataMapper {

    fun createOrderCommandToRestaurant(createOrderCommand: CreateOrderCommand): Restaurant {
        return Restaurant(
            restaurantId = RestaurantId(createOrderCommand.restaurantId),
            products = createOrderCommand.items.map { orderItem -> Product(ProductId(orderItem.productId)) }.toList()
        )
    }

    fun createOrderCommandToOrder(createOrderCommand: CreateOrderCommand): Order {
        return Order(
            customerId = CustomerId(createOrderCommand.customerId),
            restaurantId = RestaurantId(createOrderCommand.restaurantId),
            streetAddress = orderAddressToStreetAddress(createOrderCommand.address),
            price = Money(createOrderCommand.price),
            items = orderItemsToOrderItemEntities(createOrderCommand.items)
        )
    }

    private fun orderItemsToOrderItemEntities(
        items: List<food.ordering.system.order.service.domain.dto.create.OrderItem>
    ): List<OrderItem> {
        return items.map { orderItem ->
            OrderItem(
                orderId = OrderId(UUID.randomUUID()),
                product = Product(ProductId(orderItem.productId)),
                price = Money(orderItem.price),
                subTotal = Money(orderItem.subTotal),
                orderItemId = OrderItemId(UUID.randomUUID()),
                quantity = orderItem.quantity,
            )
        }
    }


    private fun orderAddressToStreetAddress(address: OrderAddress): StreetAddress {
        return StreetAddress(
            id = UUID.randomUUID(),
            street = address.street,
            postalCode = address.postalCode,
            city = address.city,
        )
    }

    fun orderToCreateOrderResponse(order: Order): CreateOrderResponse {
        return CreateOrderResponse(
            orderTrackingId = order.trackingId.value,
            orderStatus = order.orderStatus,
            message = "create order"
        )
    }
}