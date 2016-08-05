package demo.coffeeshop.workcounters

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.ClosedShape
import akka.stream.UniformFanInShape
import akka.stream.javadsl.*
import demo.coffeeshop.CoffeeShop
import demo.coffeeshop.domain.*

class CoffeeCreateCounter(val coffeeShop: CoffeeShop) {
    private val zip = ZipWith.create { shotOfCoffee: CoolableFluid, milk: CoolableFluid -> createCoffee(shotOfCoffee as ShotOfCoffee, milk as Milk) }
    val combineForCoffeeGraph = GraphDSL.create { builder ->
        val fanInShape2 = builder.add(zip)
        UniformFanInShape(fanInShape2.out(), arrayOf(fanInShape2.in0(), fanInShape2.in1()))
    }

    fun createCoffee(shotOfCoffee: ShotOfCoffee, milk: Milk): Coffee {
        shotOfCoffee.cancelTimer()
        milk.cancelTimer()
        return Coffee(shotOfCoffee, milk)
    }
}

fun main(args: Array<String>) {
    val coffeeCreateCounter = CoffeeCreateCounter(CoffeeShop())

    System.setProperty("akka.stream.materializer.max-input-buffer-size", "256")
    val system = ActorSystem.apply("MySystem")

    val materializer = ActorMaterializer.create(system)

    val order = Order(1234, CoffeeType.CAPPUCCINO)

    val coffeeCreateGraph = GraphDSL.create {
        builder ->
        val complexInlet = builder.add(coffeeCreateCounter.combineForCoffeeGraph)
        builder.from(builder.add(Source.single(ShotOfCoffee(order = order)))).toInlet(complexInlet.`in`(0))
        builder.from(builder.add(Source.single(Milk(order = order)))).toInlet(complexInlet.`in`(1))
        builder.from(complexInlet.out()).to(builder.add(Sink.foreach { print(it) }))
        ClosedShape.getInstance()
    }

    val runnableGraph = RunnableGraph.fromGraph(coffeeCreateGraph)

    runnableGraph.run(materializer)
    system.shutdown()
}
