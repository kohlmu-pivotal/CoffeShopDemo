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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import demo.coffeeshop.service.impl.CoffeeShopImpl;

public class Milk {

  private Temperature temperature = Temperature.COLD;
//  private ExecutorService coolDownService;

  private int timeNormal;

  public Milk() {
    this(CoffeeShopImpl.DEFAULT_TIME_NORMAL_IN_MILLIS);
  }

  public Milk(final int timeNormal) {
    if (timeNormal > 0) {
      this.timeNormal = timeNormal;
    }
//    coolDownService = Executors.newSingleThreadExecutor();
  }

  public boolean isCold() {
    return Temperature.HOT.equals(temperature) ? false : true;
  }

  public Milk steam() {
    temperature = Temperature.HOT;
//    coolDownService.submit(new TemperatureCoolDownThread(timeNormal));
    return this;
  }

  public void shutdown() {
//    coolDownService.shutdownNow();
  }


//  private class TemperatureCoolDownThread implements Runnable {
//
//    private final int COOL_DOWN_IN_SECONDS = 5;
//    private final int timeNormal;
//
//    public TemperatureCoolDownThread() {
//      this(CoffeeShopImpl.DEFAULT_TIME_NORMAL_IN_MILLIS);
//    }
//
//    public TemperatureCoolDownThread(final int timeNormal) {
//      this.timeNormal = timeNormal;
//    }
//
//    public void run() {
//      try {
//        Thread.currentThread().sleep(timeNormal * COOL_DOWN_IN_SECONDS);
//        temperature = Temperature.COLD;
//      } catch (InterruptedException e) {
//      }
//      shutdown();
//    }
//  }
}
