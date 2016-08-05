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
package demo.coffeeshop.service.impl;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import demo.coffeeshop.actors.BaristaWithCoffeeQueue;
import demo.coffeeshop.actors.Customer;
import demo.coffeeshop.domain.Order;

import static demo.coffeeshop.domain.Responsibility.*;

public class CoffeeShopImplWithCoffeeQueue extends CoffeeShopImpl {

  private final LinkedList<Order> coffeeOrders = new LinkedList<>();

  public CoffeeShopImplWithCoffeeQueue(final int numberOfBaristas) {
    super(numberOfBaristas);
  }

  public CoffeeShopImplWithCoffeeQueue(final int numberOfBaristas, final int timeNormal) {
    super(numberOfBaristas, timeNormal);
  }

  public void addOrderToBrewCoffeeQueue(final Order order) {
    synchronized (coffeeOrders) {
      coffeeOrders.addLast(order);
      coffeeOrders.notify();
    }
  }

  public Order getOrdersForBrewing() {
    synchronized (coffeeOrders) {
      try {
        Order order = coffeeOrders.removeFirst();
        System.out.println(Thread.currentThread().getId()+"--- Orders for brewing left: "+coffeeOrders.size());
        return order;
      } catch (NoSuchElementException e) {
        try {
          coffeeOrders.wait();
          return coffeeOrders.removeFirst();
        } catch (InterruptedException e1) {
        }
      }
    }
    return null;
  }

  public static void main(String[] args) {
    CoffeeShopImpl coffeeShop = new CoffeeShopImplWithCoffeeQueue(2,10);
    coffeeShop.addBaristas(new BaristaWithCoffeeQueue(coffeeShop, TAKE_ORDERS, FROTH_MILK));
    coffeeShop.addBaristas(new BaristaWithCoffeeQueue(coffeeShop, BREW_COFFEE, COMBINE_INGREDIENTS, SERVE));


    coffeeShop.openDoors();
    for (int count : new int[10000]) {
      coffeeShop.queueForCoffee(new Customer());
    }
    while (true) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
      }
    }
  }

}
