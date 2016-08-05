package demo.coffeeshop.workcounters

import akka.stream.javadsl.Flow
import demo.coffeeshop.CoffeeShop
import demo.coffeeshop.domain.CoffeeType
import demo.coffeeshop.domain.Order

class OrderCounter(val coffeShop: CoffeeShop) {
    private var orderNumber: Long = 0
    val orderflow = Flow.of(CoffeeType::class.java).map { coffeeType -> Order(incrementAndGetOrderNumber(), coffeeType) }

    private fun incrementAndGetOrderNumber(): Long {
        orderNumber++
        return orderNumber
    }
}