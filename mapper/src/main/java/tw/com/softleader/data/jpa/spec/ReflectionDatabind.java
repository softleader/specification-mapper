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

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;
import static org.springframework.util.ReflectionUtils.doWithLocalFields;
import static org.springframework.util.ReflectionUtils.makeAccessible;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * Databind implementation using Spring's {@code ReflectionUtils}
 *
 * @see ReflectionUtils
 */
@RequiredArgsConstructor
class ReflectionDatabind implements Databind {

  @Getter
  @NonNull
  private final Object target;

  @Getter
  @NonNull
  private final Field field;

  private final AtomicBoolean loaded = new AtomicBoolean();
  private final CountDownLatch latch = new CountDownLatch(1);
  private Object value;

  static List<Databind> of(@Nullable Object target) {
    return of(target, ReflectionDatabind::new);
  }

  static List<Databind> of(@Nullable Object target,
      @NonNull BiFunction<Object, Field, Databind> factory) {
    if (target == null) {
      return emptyList();
    }
    val lookup = new ArrayList<Databind>();
    doWithLocalFields(target.getClass(),
        field -> lookup.add(factory.apply(target, field)));
    return unmodifiableList(lookup);
  }

  @Override
  @SneakyThrows
  public Optional<Object> getFieldValue() {
    if (loaded.compareAndSet(false, true)) {
      value = getFieldValue(target, field);
      latch.countDown();
    } else {
      latch.await();
    }
    return ofNullable(value);
  }

  // Visible for testing
  Object getFieldValue(Object target, Field field) {
    makeAccessible(field);
    val val = ReflectionUtils.getField(field, target);
    if (val instanceof Optional) {
      return ((Optional<?>) val).orElse(null);
    }
    return val;
  }
}
