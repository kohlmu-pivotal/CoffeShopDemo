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

import java.util.Random;

import demo.coffeeshop.domain.Coffee;
import demo.coffeeshop.domain.CoffeeType;
import demo.coffeeshop.domain.Order;

public class Customer implements Runnable {

  private Order order;
  private Coffee coffee;
  private Boolean waitingForCoffee = new Boolean(true);

  public Customer() {
    CoffeeType[] values = CoffeeType.values();
    Random random = new Random(System.nanoTime());
    int i = random.nextInt(values.length);
    this.order = new Order(this, values[i]);
  }

  public Customer(final Order order) {
    this.order = order;
  }

  public Order getOrder() {
    return order;
  }

  public void receiveCoffee(final Coffee coffee) {
    synchronized (waitingForCoffee) {
      this.coffee = coffee;
      waitingForCoffee.notify();
    }
  }

  @Override
  public void run() {
    synchronized (waitingForCoffee) {
      if (coffee == null) {
        try {
          waitingForCoffee.wait();
          drinkCoffee();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void drinkCoffee() {
    String response = coffee.isCold() ? "Customer is unhappy, their coffee is cold" : "Customer is happy that their coffee is hot";
    System.out.println(response);
  }
}
