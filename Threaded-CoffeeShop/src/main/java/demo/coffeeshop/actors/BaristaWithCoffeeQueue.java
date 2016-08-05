/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demo.coffeeshop.actors;

import demo.coffeeshop.domain.Coffee;
import demo.coffeeshop.domain.Order;
import demo.coffeeshop.domain.Responsibility;
import demo.coffeeshop.service.CoffeeShop;
import demo.coffeeshop.service.impl.CoffeeShopImplWithCoffeeQueue;

public class BaristaWithCoffeeQueue extends Barista {

  public BaristaWithCoffeeQueue(final CoffeeShop coffeeShop, final Responsibility... responsibilities) {
    super(coffeeShop, responsibilities);
  }

  @Override
  public void run() {
    while (true) {
      try {
        Order order = null;
        Coffee coffee = null;
        if (getResponsibilitiesList().contains(Responsibility.TAKE_ORDERS)) {
          Customer customer = getCoffeeShop().nextCustomer();
          order = customer.getOrder();
          ((CoffeeShopImplWithCoffeeQueue) getCoffeeShop()).addOrderToBrewCoffeeQueue(order);

        }
        if (getResponsibilitiesList().contains(Responsibility.BREW_COFFEE)) {
          order = ((CoffeeShopImplWithCoffeeQueue) getCoffeeShop()).getOrdersForBrewing();
          getCoffeeShop().addBrewedCoffee(getCoffeeMachine().brewCoffee());
        }
        if (getResponsibilitiesList().contains(Responsibility.FROTH_MILK)) {
          getCoffeeShop().addSteamedMilk(getCoffeeMachine().steamMilk());
        }
        if (getResponsibilitiesList().contains(Responsibility.COMBINE_INGREDIENTS)) {
          coffee = new Coffee(order.getCoffeeType());
          coffee.addMilk(getCoffeeShop().getSteamedMilk());
          coffee.addShotOfCoffee(getCoffeeShop().getShotOfCoffee());
        }
        if (getResponsibilitiesList().contains(Responsibility.SERVE)) {
          getCoffeeShop().completeOrder(order, coffee);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
