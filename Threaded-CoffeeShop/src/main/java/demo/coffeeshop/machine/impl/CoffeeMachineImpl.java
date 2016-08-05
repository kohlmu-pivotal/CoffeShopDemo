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
package demo.coffeeshop.machine.impl;

import demo.coffeeshop.domain.Milk;
import demo.coffeeshop.domain.ShotOfCoffee;
import demo.coffeeshop.machine.CoffeeMachine;
import demo.coffeeshop.service.CoffeeShop;

public class CoffeeMachineImpl implements CoffeeMachine {

  private static final int TIME_TO_MAKE_COFFEE = 30;  //The time it takes seconds to brew coffee
  private static final int TIME_TO_STEAM_MILK = 15;   //The time it takes seconds to steam milk
  private final CoffeeShop coffeeShop;

  private Object brewLock = new Object();
  private Object steamLock = new Object();

  public CoffeeMachineImpl(final CoffeeShop coffeeShop) {
    this.coffeeShop = coffeeShop;
  }

  @Override
  public ShotOfCoffee brewCoffee() {
    synchronized (brewLock) {
      try {
        Thread.currentThread().sleep(TIME_TO_MAKE_COFFEE * coffeeShop.getTimeNormal());
        ShotOfCoffee shotOfCoffee = new ShotOfCoffee(coffeeShop.getTimeNormal());
        brewLock.notify();
        return shotOfCoffee;
      } catch (InterruptedException e) {
        throw new RuntimeException("Something went wrong when brewing the coffee", e);
      }
    }
  }

  @Override
  public Milk steamMilk() {
    synchronized (steamLock) {
      long id = Thread.currentThread().getId();
      try {
        Milk milk = new Milk(coffeeShop.getTimeNormal());
        Thread.currentThread().sleep(TIME_TO_STEAM_MILK * coffeeShop.getTimeNormal());
        milk.steam();
        steamLock.notify();
        return milk;
      } catch (InterruptedException e) {
        throw new RuntimeException("Something went wrong when brewing the coffee", e);
      }
    }
  }
}
