package com.food.ordering.system.restaurant.service.domain.mapper

import com.food.ordering.system.domain.valueobject.Money
import com.food.ordering.system.domain.valueobject.OrderId
import com.food.ordering.system.domain.valueobject.OrderStatus
import com.food.ordering.system.domain.valueobject.RestaurantId
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest
import com.food.ordering.system.restaurant.service.domain.entity.OrderDetail
import com.food.ordering.system.restaurant.service.domain.entity.Product
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RestaurantDataMapper {
    fun restaurantApprovalRequestToRestaurant(restaurantApprovalRequest: RestaurantApprovalRequest): Restaurant {
        return Restaurant(
            id = RestaurantId(UUID.fromString(restaurantApprovalRequest.restaurantId)),
            orderDetail = OrderDetail(
                id = OrderId(UUID.fromString(restaurantApprovalRequest.orderId)),
                products = restaurantApprovalRequest.projects.map { product ->
                    Product(
                        id = product.id,
                        quantity = product.quantity
                    )
                },
                orderStatus = OrderStatus.valueOf(restaurantApprovalRequest.restaurantOrderStatus.name),
                totalAmount = Money(restaurantApprovalRequest.price)
            ),
        )
    }
}
