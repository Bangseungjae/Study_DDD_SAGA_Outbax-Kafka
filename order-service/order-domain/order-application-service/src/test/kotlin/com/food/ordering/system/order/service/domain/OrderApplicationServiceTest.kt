package com.food.ordering.system.order.service.domain

import com.food.ordering.system.domain.exception.DomainException
import com.food.ordering.system.domain.valueobject.*
import com.food.ordering.system.order.service.domain.entity.Customer
import com.food.ordering.system.order.service.domain.entity.Order
import com.food.ordering.system.order.service.domain.entity.Product
import com.food.ordering.system.order.service.domain.entity.Restaurant
import com.food.ordering.system.order.service.domain.exception.OrderDomainException
import food.ordering.system.order.service.domain.dto.create.CreateOrderCommand
import food.ordering.system.order.service.domain.dto.create.OrderAddress
import food.ordering.system.order.service.domain.dto.create.OrderItem
import food.ordering.system.order.service.domain.mapper.OrderDataMapper
import food.ordering.system.order.service.domain.ports.input.service.OrderApplicationService
import food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository
import food.ordering.system.order.service.domain.ports.output.repository.OrderRepository
import food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.util.UUID


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = [OrderTestConfiguration::class])
class OrderApplicationServiceTest {

    @Autowired
    private lateinit var orderApplicationService: OrderApplicationService

    @Autowired
    private lateinit var orderDataMapper: OrderDataMapper

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var restaurantRepository: RestaurantRepository

    private lateinit var createOrderCommand: CreateOrderCommand
    private lateinit var createOrderCommandWrongPrice: CreateOrderCommand
    private lateinit var createOrderCommandWrongProductPrice: CreateOrderCommand
    private val CUSTOMER_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb41")
    private val RESTAURANT_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb41")
    private val PRODUCT_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb41")
    private val ORDER_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb41")
    private val PRICE: BigDecimal = BigDecimal("200.00")


    @BeforeAll
    fun init() {
        createOrderCommand = CreateOrderCommand(
            customerId = CUSTOMER_ID,
            restaurantId = RESTAURANT_ID,
            address = OrderAddress(
                street = "street_1",
                postalCode = "1000AB",
                city = "Paris"
            ),
            price = PRICE,
            items = listOf(
                OrderItem(
                    productId = PRODUCT_ID,
                    quantity = 1,
                    price = BigDecimal("50.00"),
                    subTotal = BigDecimal("50.00")
                ),
                OrderItem(
                    productId = PRODUCT_ID,
                    quantity = 3,
                    price = BigDecimal("50.00"),
                    subTotal = BigDecimal("150.00")
                ),
            )
        )
        createOrderCommandWrongPrice = CreateOrderCommand(
            customerId = CUSTOMER_ID,
            restaurantId = RESTAURANT_ID,
            address = OrderAddress(
                street = "street_1",
                postalCode = "1000AB",
                city = "Paris"
            ),
            price = BigDecimal("250.00"),
            items = listOf(
                OrderItem(
                    productId = PRODUCT_ID,
                    quantity = 1,
                    price = BigDecimal("50.00"),
                    subTotal = BigDecimal("50.00"),
                ),
                OrderItem(
                    productId = PRODUCT_ID,
                    quantity = 3,
                    price = BigDecimal("50.00"),
                    subTotal = BigDecimal("150.00"),
                ),
            )
        )

        createOrderCommandWrongProductPrice = CreateOrderCommand(
            customerId = CUSTOMER_ID,
            restaurantId = RESTAURANT_ID,
            address = OrderAddress(
                street = "street_1",
                postalCode = "1000AB",
                city = "Paris"
            ),
            price = BigDecimal("210.00"),
            items = listOf(
                OrderItem(
                    productId = PRODUCT_ID,
                    quantity = 1,
                    price = BigDecimal("60.00"),
                    subTotal = BigDecimal("60.00"),
                ),
                OrderItem(
                    productId = PRODUCT_ID,
                    quantity = 3,
                    price = BigDecimal("50.00"),
                    subTotal = BigDecimal("150.00"),
                ),
            )
        )

        val customer: Customer = Customer(CustomerId(CUSTOMER_ID))
        val restaurantResponse: Restaurant = Restaurant(
            restaurantId = RestaurantId(createOrderCommand.restaurantId),
            products = listOf(
                Product(productId = ProductId(PRODUCT_ID), name = "product-1", price = Money(BigDecimal("50.00"))),
                Product(productId = ProductId(PRODUCT_ID), name = "product-2", price = Money(BigDecimal("50.00"))),
            ),
            active = true
        )
        val order: Order = orderDataMapper.createOrderCommandToOrder(createOrderCommand)
        order.id = OrderId(ORDER_ID)

        `when`(customerRepository.findCustomer(CUSTOMER_ID)).thenReturn(customer)

        `when`(
            restaurantRepository.findRestaurantInformation(
                orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)
            )
        ).thenReturn(restaurantResponse)

        `when`(orderRepository.save(anyOrNull<Order>()))
            .thenReturn(order)
    }


    @Test
    fun testCreateOrder() {
        val createOrderResponse = orderApplicationService.createOrder(createOrderCommand)
        assertEquals(createOrderResponse.orderStatus, OrderStatus.PENDING)
        assertEquals(createOrderResponse.message, "Order created successfully")
        assertNotNull(createOrderResponse.orderTrackingId)
    }

    @Test
    fun testCreateOrderWithWrongTotalPrice() {
        assertThatThrownBy {  orderApplicationService.createOrder(createOrderCommandWrongPrice) }
            .isInstanceOf(DomainException::class.java)
            .hasMessage("Total price: 250.00 is not equal to Order items total: 200.00!")
    }

    @Test
    fun testCreateOrderWithWrongProductPrice() {
        assertThatThrownBy { orderApplicationService.createOrder(createOrderCommandWrongProductPrice) }
            .isInstanceOf(DomainException::class.java)
            .hasMessage("Order item price: 60.00 is not valid for product $PRODUCT_ID")
    }

    @Test
    fun testCreateOrderWithPassiveRestaurant() {
        val restaurantResponse: Restaurant = Restaurant(
            restaurantId = RestaurantId(createOrderCommand.restaurantId),
            products = listOf(
                Product(productId = ProductId(PRODUCT_ID), name = "product-1", price = Money(BigDecimal("50.00"))),
                Product(productId = ProductId(PRODUCT_ID), name = "product-2", price = Money(BigDecimal("50.00"))),
            ),
            active = false
        )

        `when`(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
            .thenReturn(restaurantResponse)

        assertThatThrownBy { orderApplicationService.createOrder(createOrderCommand) }
            .isInstanceOf(DomainException::class.java)
            .hasMessage("Restaurant with id $RESTAURANT_ID is currently not active!")
    }
}
