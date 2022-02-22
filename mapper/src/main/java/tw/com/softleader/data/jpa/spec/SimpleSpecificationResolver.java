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

import static java.util.Optional.of;
import static org.springframework.util.ReflectionUtils.accessibleConstructor;

import java.util.stream.StreamSupport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.And;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.Not;
import tw.com.softleader.data.jpa.spec.domain.Or;
import tw.com.softleader.data.jpa.spec.domain.SimpleSpecification;

/**
 * @author Matt Ho
 */
@Slf4j
class SimpleSpecificationResolver implements SpecificationResolver {

  @Override
  public boolean supports(@NonNull Databind databind) {
    return databind.getField().isAnnotationPresent(Spec.class);
  }

  @Override
  public Specification<Object> buildSpecification(@NonNull Context context,
      @NonNull Databind databind) {
    var def = databind.getField().getAnnotation(Spec.class);
    log.debug("Building specification of [{}.{}] for {}",
        databind.getTarget().getClass().getSimpleName(),
        databind.getField().getName(),
        def);
    return databind.getFieldValue()
        .filter(this::valuePresent)
        .map(value -> {
          var path = of(def.path())
              .filter(StringUtils::hasText)
              .orElseGet(databind.getField()::getName);
          var spec = newSimpleSpecification(context, def.value(), path, value);
          if (def.not()) {
            spec = new Not<>(spec);
          }
          if (databind.getField().isAnnotationPresent(
              tw.com.softleader.data.jpa.spec.annotation.And.class)) {
            return new And<>(spec);
          }
          if (databind.getField().isAnnotationPresent(tw.com.softleader.data.jpa.spec.annotation.Or.class)) {
            return new Or<>(spec);
          }
          return spec;
        }).orElseGet(() -> {
          log.debug("Value of [{}.{}] is null or empty, skipping it",
              databind.getTarget().getClass().getSimpleName(),
              databind.getField().getName());
          return null;
        });
  }

  boolean valuePresent(Object value) {
    if (value instanceof Iterable) {
      return StreamSupport.stream(((Iterable<?>) value).spliterator(), false).count() > 0;
    }
    return true;
  }

  @SneakyThrows
  private Specification<Object> newSimpleSpecification(
      @NonNull Context context,
      @NonNull Class<? extends SimpleSpecification> domainClass,
      @NonNull String path,
      @NonNull Object value) {
    return accessibleConstructor(domainClass, Context.class, String.class, Object.class)
        .newInstance(context, path, value);
  }
}
