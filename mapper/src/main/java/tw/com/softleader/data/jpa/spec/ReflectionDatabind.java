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

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.springframework.util.ReflectionUtils.doWithLocalFields;
import static org.springframework.util.ReflectionUtils.makeAccessible;
import static tw.com.softleader.data.jpa.spec.FieldDescriptor.ABSENT;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * Databind implementation using Spring's {@code ReflectionUtils}
 *
 * @see ReflectionUtils
 */
@RequiredArgsConstructor
class ReflectionDatabind implements Databind {

  @Getter @NonNull private final Object target;
  @Getter @NonNull private final FieldDescriptor field;
  @NonNull private final SkippingStrategy skippingStrategy;

  private final AtomicBoolean loaded = new AtomicBoolean();
  private final CountDownLatch latch = new CountDownLatch(1);
  private Object value;

  ReflectionDatabind(
      @NonNull Object target, @NonNull Field field, @NonNull SkippingStrategy skippingStrategy) {
    this(target, new ReflectiveFieldDescriptor(field), skippingStrategy);
  }

  ReflectionDatabind(@NonNull Object target, @NonNull SkippingStrategy skippingStrategy) {
    this(target, ABSENT, skippingStrategy);
  }

  static List<Databind> of(@NonNull Object target, @NonNull SkippingStrategy skippingStrategy) {
    return of(target, skippingStrategy, ReflectionDatabind::new, ReflectionDatabind::new);
  }

  // Visible for testing
  static List<Databind> of(
      @NonNull Object target,
      @NonNull SkippingStrategy skippingStrategy,
      @NonNull DatabindFactory factory,
      @NonNull DatabindFactoryNoField factoryNoField) {
    var lookup = new ArrayList<Databind>();
    lookup.add(factoryNoField.apply(target, skippingStrategy));
    doWithLocalFields(
        target.getClass(), field -> lookup.add(factory.apply(target, field, skippingStrategy)));
    return unmodifiableList(lookup);
  }

  @Override
  @SneakyThrows
  public Optional<Object> getFieldValue() {
    if (loaded.compareAndSet(false, true)) {
      value = getFieldValue(target, field.unwrap(Field.class));
      latch.countDown();
    } else {
      latch.await();
    }
    return ofNullable(value).filter(not(skippingStrategy::shouldSkip));
  }

  // Visible for testing
  Object getFieldValue(@NonNull Object target, @Nullable Field field) {
    if (field == null) {
      return null;
    }
    makeAccessible(field);
    var val = ReflectionUtils.getField(field, target);
    if (val instanceof Optional) {
      return ((Optional<?>) val).orElse(null);
    }
    return val;
  }

  // for test spy
  @FunctionalInterface
  interface DatabindFactory {

    Databind apply(Object target, Field field, SkippingStrategy strategy);
  }

  // for test spy
  @FunctionalInterface
  interface DatabindFactoryNoField {

    Databind apply(Object target, SkippingStrategy strategy);
  }
}
