/*
 * Copyright © 2022 SoftLeader
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package tw.com.softleader.data.jpa.spec;

import static java.util.concurrent.Executors.newFixedThreadPool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

class ReflectionDatabindTest {

  @SneakyThrows
  @Test
  void fireOnlyOnce() {

    var object = new MyObject("hello", null, Optional.empty(), Arrays.asList());
    var databind = ReflectionDatabind.of(object,
        new DefaultSkippingStrategy(),
        (obj, field, strategy) -> spy(new ReflectionDatabind(obj, field, strategy)));

    assertThat(databind)
        .hasSize(4);

    var numberOfThreads = 100;
    var service = newFixedThreadPool(numberOfThreads);
    var latch = new CountDownLatch(numberOfThreads);
    for (int i = 0; i < numberOfThreads; i++) {
      service.submit(() -> {
        databind.forEach(Databind::getFieldValue);
        latch.countDown();
      });
    }
    latch.await();

    databind.forEach(bind -> {
      assertThat(bind)
          .isNotNull()
          .isInstanceOf(ReflectionDatabind.class);

      verify((ReflectionDatabind) bind, times(1))
          .getFieldValue(eq(object), any(Field.class));
    });
  }

  @AllArgsConstructor
  static class MyObject {

    String a;
    Integer b;
    Optional<Long> c;
    Collection<String> d;
  }

}
