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
package demo.coffeeshop.domain;

import demo.coffeeshop.actors.Customer;

public class Order {

  private final CoffeeType coffeeType;
  private final Customer customer;
  private Coffee coffee;

  public Order(final Customer customer, final CoffeeType coffeeType) {
    this.coffeeType = coffeeType;
    this.customer = customer;
  }

  public CoffeeType getCoffeeType() {
    return coffeeType;
  }

  public Customer getCustomer() {
    return customer;
  }

  public void addCoffee(Coffee coffee) {
    this.coffee = coffee;
  }

  public Coffee getCoffee() {
    return coffee;
  }
}
