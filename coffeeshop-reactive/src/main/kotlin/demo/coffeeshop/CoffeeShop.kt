package demo.coffeeshop

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.FlowShape
import akka.stream.javadsl.*
import demo.coffeeshop.domain.CoffeeType
import demo.coffeeshop.domain.Milk
import demo.coffeeshop.domain.Order
import demo.coffeeshop.domain.ShotOfCoffee
import demo.coffeeshop.machine.CoffeeMachineImpl
import demo.coffeeshop.workcounters.CoffeeCreateCounter
import demo.coffeeshop.workcounters.OrderCounter
import java.util.*

class CoffeeShop(val timeNormal: Int = 1000) {
    val coffeeMachine: CoffeeMachineImpl = CoffeeMachineImpl(this)
    val orderCounter = OrderCounter(this)
    val coffeeCreateCounter = CoffeeCreateCounter(this)

    val coffeeShopGraph = GraphDSL.create { builder ->
        val orderCounter = builder.add(orderCounter.orderflow)
        val broadcaster = builder.add(Broadcast.create<Order>(2))
        val steamGraph = builder.add(coffeeMachine.steamGraph)
        val brewGraph = builder.add(coffeeMachine.brewGraph)
        val combineForCoffeeShape = builder.add(coffeeCreateCounter.combineForCoffeeGraph)


        builder.from(orderCounter).toInlet(broadcaster.`in`())
        builder.from(broadcaster).via<ShotOfCoffee>(brewGraph)
        builder.from(broadcaster).via<Milk>(steamGraph)
        builder.from(brewGraph).toInlet(combineForCoffeeShape.`in`(0))
        builder.from(steamGraph).toInlet(combineForCoffeeShape.`in`(1))
        FlowShape.of(orderCounter.`in`(), combineForCoffeeShape.out())
    }

    val coffeeShopGraph2Workers = GraphDSL.create { builder ->
        val orderCounter = builder.add(orderCounter.orderflow)
        val broadcaster = builder.add(Broadcast.create<Order>(2))
        val combineForCoffeeShape = builder.add(coffeeCreateCounter.combineForCoffeeGraph)

        val brewBalancer = builder.add(Balance.create<Order>(2))
        val brewMerger = builder.add(Merge.create<ShotOfCoffee>(2))
        val steamBalancer = builder.add(Balance.create<Order>(2))
        val steamMerger = builder.add(Merge.create<Milk>(2))

        builder.from(orderCounter).toInlet(broadcaster.`in`())
        builder.from(broadcaster).toInlet(brewBalancer.`in`())
        builder.from(broadcaster).toInlet(steamBalancer.`in`())

        builder.from(steamBalancer).via<Milk>(builder.add(coffeeMachine.steamGraph)).toFanIn<Milk>(steamMerger)
        builder.from(steamBalancer).via<Milk>(builder.add(coffeeMachine.steamGraph)).toFanIn<Milk>(steamMerger)

        builder.from(brewBalancer).via<ShotOfCoffee>(builder.add(coffeeMachine.brewGraph)).toFanIn<ShotOfCoffee>(brewMerger)
        builder.from(brewBalancer).via<ShotOfCoffee>(builder.add(coffeeMachine.brewGraph)).toFanIn<ShotOfCoffee>(brewMerger)


        builder.from(brewMerger).toInlet(combineForCoffeeShape.`in`(0))
        builder.from(steamMerger).toInlet(combineForCoffeeShape.`in`(1))
        FlowShape.of(orderCounter.`in`(), combineForCoffeeShape.out())
    }

    val coffeeShopGraph5Workers = GraphDSL.create { builder ->
        val orderCounter = builder.add(orderCounter.orderflow)
        val broadcaster = builder.add(Broadcast.create<Order>(2))
        val combineForCoffeeShape = builder.add(coffeeCreateCounter.combineForCoffeeGraph)

        val brewBalancer = builder.add(Balance.create<Order>(2))
        val brewMerger = builder.add(Merge.create<ShotOfCoffee>(2))
        val steamBalancer = builder.add(Balance.create<Order>(2))
        val steamMerger = builder.add(Merge.create<Milk>(2))

        builder.from(orderCounter).toInlet(broadcaster.`in`())
        builder.from(broadcaster).toInlet(brewBalancer.`in`())
        builder.from(broadcaster).toInlet(steamBalancer.`in`())

        builder.from(steamBalancer).via<Milk>(builder.add(coffeeMachine.steamGraph)).toFanIn<Milk>(steamMerger)
        builder.from(steamBalancer).via<Milk>(builder.add(coffeeMachine.steamGraph)).toFanIn<Milk>(steamMerger)

        builder.from(brewBalancer).via<ShotOfCoffee>(builder.add(coffeeMachine.brewGraph)).toFanIn<ShotOfCoffee>(brewMerger)
        builder.from(brewBalancer).via<ShotOfCoffee>(builder.add(coffeeMachine.brewGraph)).toFanIn<ShotOfCoffee>(brewMerger)


        builder.from(brewMerger).toInlet(combineForCoffeeShape.`in`(0))
        builder.from(steamMerger).toInlet(combineForCoffeeShape.`in`(1))
        FlowShape.of(orderCounter.`in`(), combineForCoffeeShape.out())
    }
}

private val MAX_ITERTATIONS = 10000

fun main(args: Array<String>) {
    val coffeeShop = CoffeeShop(1)
    val system = ActorSystem.apply("MySystem")

    val materializer = ActorMaterializer.create(system)

    val arrayList = ArrayList<CoffeeType>(MAX_ITERTATIONS)
    for (count in 1..MAX_ITERTATIONS) {
        arrayList.add(CoffeeType.CAPPUCCINO)
    }

    Source.from(arrayList).via(coffeeShop.coffeeShopGraph5Workers).to(Sink.foreach {
        val coffee = it
        println("Coffee for Order: (Coffee:${coffee.shotOfCoffee.order.orderNumber} / Milk:${coffee.milk.order.orderNumber}) is ${if (coffee.isHot()) "HOT" else "COLD"} and the ShotOfCoffee is ${coffee.shotOfCoffee.temperature} and the Milk is ${coffee.milk.temperature}")
    }).run(materializer)
}



