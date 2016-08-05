package demo.coffeeshop.domain

abstract class CoolableFluid(val timeNormal: Int = 1000) {
    var temperature: Temperature = Temperature.COLD
    protected abstract fun getCoolDownConstant(): Int
//    lateinit var coolDownTimer: Timer

    protected fun startCoolDown() {
//        coolDownTimer = timer(period = 1, initialDelay = (getCoolDownConstant() * timeNormal).toLong(), daemon = true, action = {
//            temperature = Temperature.COLD
//            this.cancel()
//        })
    }

    fun cancelTimer() {
//        coolDownTimer.cancel()
    }

    fun isHot(): Boolean {
        return Temperature.HOT == temperature
    }
}

class Milk(timeNormal: Int = 1000, val order: Order) : CoolableFluid(timeNormal) {
    companion object {
        private val COOL_DOWN_CONSTANT: Int = 45
    }

    override fun getCoolDownConstant(): Int {
        return COOL_DOWN_CONSTANT
    }

    fun steam(): Milk {
        temperature = Temperature.HOT
        startCoolDown()
        return this
    }
}

class ShotOfCoffee(timeNormal: Int = 1000, val order: Order) : CoolableFluid(timeNormal) {
    init {
        temperature = Temperature.HOT
        startCoolDown()
    }

    companion object {
        private val COOL_DOWN_CONSTANT: Int = 35
    }

    override fun getCoolDownConstant(): Int {
        return COOL_DOWN_CONSTANT
    }
}

enum class Temperature() {
    HOT, COLD
}

enum class CoffeeType {
    CAPPUCCINO, ESPRESSO, LATTE
}

data class Order(val orderNumber: Long, val coffeeType: CoffeeType) {
}


data class Coffee(val shotOfCoffee: ShotOfCoffee, val milk: Milk) {
    fun isHot(): Boolean {
        return shotOfCoffee.isHot() && milk.isHot()
    }
}
