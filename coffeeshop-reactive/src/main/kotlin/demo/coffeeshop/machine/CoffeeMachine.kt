package demo.coffeeshop.machine

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.FlowShape
import akka.stream.ThrottleMode
import akka.stream.javadsl.Flow
import akka.stream.javadsl.GraphDSL
import akka.stream.javadsl.Sink
import akka.stream.javadsl.Source
import demo.coffeeshop.CoffeeShop
import demo.coffeeshop.domain.CoffeeType
import demo.coffeeshop.domain.Milk
import demo.coffeeshop.domain.Order
import demo.coffeeshop.domain.ShotOfCoffee
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit


interface CoffeeMachine {
    fun brewCoffee(order: Order): ShotOfCoffee
    fun steamMilk(order: Order): Milk
}

class CoffeeMachineImpl(val coffeeShop: CoffeeShop) : CoffeeMachine {

    companion object {
        private val BREW_TIME: Int = 30
        private val STEAM_TIME: Int = 15
    }

    val timeNormal = coffeeShop.timeNormal

    private val brewFlow = Flow.of(Order::class.java)
            .throttle(1, FiniteDuration(BREW_TIME * timeNormal.toLong(), TimeUnit.MILLISECONDS), 1, ThrottleMode.shaping())
            .map { brewCoffee(it) }

    private val steamFlow = Flow.of(Order::class.java)
            .throttle(1, FiniteDuration(STEAM_TIME * timeNormal.toLong(), TimeUnit.MILLISECONDS), 1, ThrottleMode.shaping())
            .map { steamMilk(it) }


    val brewGraph = GraphDSL.create { builder ->
        val brewFlowShape = builder.add(brewFlow)
        FlowShape.of(brewFlowShape.`in`(), brewFlowShape.out())
    }

    val steamGraph = GraphDSL.create { builder ->
        val steamFlowShape = builder.add(steamFlow)
        FlowShape.of(steamFlowShape.`in`(), steamFlowShape.out())
    }

    override fun brewCoffee(order: Order): ShotOfCoffee {
        return ShotOfCoffee(coffeeShop.timeNormal, order)
    }

    override fun steamMilk(order: Order): Milk {
        return Milk(coffeeShop.timeNormal, order).steam()
    }
}


fun main(args: Array<String>) {
    val coffeeMachineImpl = CoffeeMachineImpl(CoffeeShop(10))
    val system = ActorSystem.apply("MySystem")

    val materializer = ActorMaterializer.create(system)

    Source.single(Order(123, CoffeeType.CAPPUCCINO)).via(coffeeMachineImpl.brewGraph).to(Sink.foreach {
        println(it.temperature)
        println(it)
    }).run(materializer)

    Source.single(Order(123, CoffeeType.CAPPUCCINO)).via(coffeeMachineImpl.steamGraph).to(Sink.foreach {
        println(it.temperature)
        println(it)
    }).run(materializer)

    system.shutdown()
}
