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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import demo.coffeeshop.actors.Barista;
import demo.coffeeshop.domain.Coffee;
import demo.coffeeshop.domain.Milk;
import demo.coffeeshop.domain.Order;
import demo.coffeeshop.domain.Responsibility;
import demo.coffeeshop.domain.ShotOfCoffee;
import demo.coffeeshop.machine.impl.CoffeeMachineImpl;
import demo.coffeeshop.service.CoffeeShop;
import demo.coffeeshop.actors.Customer;

public class CoffeeShopImpl implements CoffeeShop {

  public static final int DEFAULT_TIME_NORMAL_IN_MILLIS = 1000;
  private CoffeeMachineImpl coffeeMachine;
  private final ExecutorService baristas;
  private final ExecutorService customerThreads;
  private LinkedList<Customer> customers = new LinkedList<>();
  private LinkedList<ShotOfCoffee> shotsOfCoffee = new LinkedList<>();
  private LinkedList<Milk> steamedMilk = new LinkedList<>();

  private int timeNormal;

  public CoffeeShopImpl(final int numberOfBaristas) {
    this(numberOfBaristas, DEFAULT_TIME_NORMAL_IN_MILLIS);
  }

  public CoffeeShopImpl(final int numberOfBaristas, final int timeNormal) {
    setTimeNormal(timeNormal);
    this.coffeeMachine = new CoffeeMachineImpl(this);
    customerThreads = Executors.newFixedThreadPool(10);
    baristas = Executors.newFixedThreadPool(numberOfBaristas);
  }

  private void setTimeNormal(final int timeNormal) {
    if (timeNormal > 0) {
      this.timeNormal = timeNormal;
    } else {
      this.timeNormal = DEFAULT_TIME_NORMAL_IN_MILLIS;
    }
  }

  public void queueForCoffee(final Customer customer) {
    joinTheQueue(customer);
    customerThreads.submit(customer);
  }

  private void joinTheQueue(final Customer customer) {
    synchronized (customers) {
      customers.addLast(customer);
      customers.notify();
    }
  }

  @Override
  public int getTimeNormal() {
    return timeNormal;
  }

  @Override
  public Customer nextCustomer() {
    synchronized (customers) {
      if (customers.size() == 0) {
        try {
          customers.wait();
        } catch (InterruptedException e) {
        }
      }
      return customers.removeFirst();
    }
  }

  @Override
  public void addBrewedCoffee(final ShotOfCoffee shotOfCoffee) {
    synchronized (shotsOfCoffee) {
      shotsOfCoffee.addLast(shotOfCoffee);
      shotsOfCoffee.notify();
    }
  }

  @Override
  public void addSteamedMilk(final Milk milk) {
    synchronized (steamedMilk) {
      steamedMilk.addLast(milk);
      steamedMilk.notify();
    }
  }

  @Override
  public Milk getSteamedMilk() {
    Milk milk = getOrWaitForObject(steamedMilk);
    System.out.println(Thread.currentThread().getId() + "--- Steamed Milk left in queue: "+steamedMilk.size());
    return milk;
  }

  @Override
  public ShotOfCoffee getShotOfCoffee() {
    ShotOfCoffee shotOfCoffee = getOrWaitForObject(shotsOfCoffee);
    return shotOfCoffee;

  }

  @Override
  public void completeOrder(final Order order, final Coffee coffee) {
    order.getCustomer().receiveCoffee(coffee);
  }

  @Override
  public void openDoors() {
  }

  @Override
  public void addBaristas(final Barista barista) {
    barista.setCoffeeMachine(coffeeMachine);
    baristas.submit(barista);
  }


  private <T> T getOrWaitForObject(final LinkedList<T> listOfInterest) {
    synchronized (listOfInterest) {
      try {
        return listOfInterest.removeFirst();
      } catch (Exception exception) {
        try {
          listOfInterest.wait();
          return listOfInterest.removeFirst();
        } catch (InterruptedException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      }
    }
  }

  public static void main(String[] args) {
    CoffeeShopImpl coffeeShop = new CoffeeShopImpl(2, 100);
    for (int i = 0; i < 2; i++) {
      coffeeShop.addBaristas(new Barista(coffeeShop, Responsibility.TAKE_ORDERS, Responsibility.BREW_COFFEE, Responsibility.FROTH_MILK, Responsibility.COMBINE_INGREDIENTS, Responsibility.SERVE));
    }
    coffeeShop.openDoors();
    for (int count : new int[10]) {
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
