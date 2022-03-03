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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import tw.com.softleader.data.jpa.spec.annotation.Join;
import tw.com.softleader.data.jpa.spec.annotation.Join.Joins;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Context;

/**
 * @author Matt Ho
 */
@Slf4j
class JoinSpecificationResolver implements SpecificationResolver {

  @Override
  public boolean supports(@NonNull Databind databind) {
    return (databind.getField().isAnnotationPresent(Join.class)
        || databind.getField().isAnnotationPresent(Joins.class));
  }

  @Override
  public Specification<Object> buildSpecification(@NonNull Context context,
      @NonNull Databind databind) {
    return databind.getFieldValue()
        .filter(this::valuePresent)
        .map(value -> {
          val specs = Stream.concat(
              joinDef(context, databind.getField()),
              joinsDef(context, databind.getField()))
              .filter(Objects::nonNull)
              .collect(toList());
          if (specs.size() == 1) {
            return specs.get(0);
          }
          return new Conjunction<>(specs);
        }).orElse(null);
  }

  boolean valuePresent(Object value) {
    if (value instanceof Iterable) {
      return StreamSupport.stream(((Iterable<?>) value).spliterator(), false).count() > 0;
    }
    return true;
  }

  private Stream<Specification<Object>> joinsDef(Context context, Field field) {
    if (!field.isAnnotationPresent(Joins.class)) {
      return Stream.empty();
    }
    return stream(field.getAnnotation(Joins.class).value())
        .map(def -> newJoin(context, def));
  }

  private Stream<Specification<Object>> joinDef(Context context, Field field) {
    if (!field.isAnnotationPresent(Join.class)) {
      return Stream.empty();
    }
    return Stream.of(
        newJoin(context, field.getAnnotation(Join.class)));
  }

  Specification<Object> newJoin(@NonNull Context context, @NonNull Join def) {
    return new tw.com.softleader.data.jpa.spec.domain.Join<>(
        context,
        def.path(),
        def.alias(),
        def.joinType(),
        def.distinct());
  }
}
