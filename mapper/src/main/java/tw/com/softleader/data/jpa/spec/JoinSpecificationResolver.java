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
import static java.util.Optional.ofNullable;
import static tw.com.softleader.data.jpa.spec.domain.JoinContext.CTX_JOIN;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import tw.com.softleader.data.jpa.spec.annotation.Join;
import tw.com.softleader.data.jpa.spec.annotation.Join.Joins;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.JoinContext;

/**
 * A {@link SpecificationResolver} that processes fields annotated with {@link Join} or {@link
 * Joins}.
 *
 * @author Matt Ho
 */
@Slf4j
class JoinSpecificationResolver implements SpecificationResolver {

  @Override
  public boolean supports(@NonNull Databind databind) {
    return isAnnotationPresentOnFieldOrTargetClass(databind, Join.class)
        || isAnnotationPresentOnFieldOrTargetClass(databind, Joins.class);
  }

  private boolean isAnnotationPresentOnFieldOrTargetClass(
      @NonNull Databind databind, Class<? extends Annotation> annotation) {
    return databind.getField().isAnnotationPresent(annotation)
        || databind.getTarget().getClass().isAnnotationPresent(annotation);
  }

  @Override
  public Specification<Object> buildSpecification(
      @NonNull Context context, @NonNull Databind databind) {
    var jc = context.getAs(CTX_JOIN, JoinContext.class);
    var specs =
        Stream.concat(
                joinsDefOnTarget(context, jc, databind), joinsDefOnField(context, jc, databind))
            .toList();
    if (specs.isEmpty()) {
      return null;
    }
    if (specs.size() == 1) {
      return specs.get(0);
    }
    return new Conjunction<>(specs);
  }

  private Stream<Specification<Object>> joinsDefOnField(
      @NonNull Context context, @NonNull JoinContext jc, @NonNull Databind databind) {
    if (databind.getFieldValue().isEmpty()) {
      return Stream.empty();
    }
    return Stream.concat(
            ofNullable(databind.getField().getAnnotation(Join.class)).stream(),
            ofNullable(databind.getField().getAnnotation(Joins.class)).stream()
                .flatMap(def -> stream(def.value())))
        .filter(def -> !jc.hasHandled(def, databind.getTarget(), databind.getField()))
        .map(
            def -> {
              try {
                return newJoin(context, def);
              } finally {
                jc.markHandled(def, databind.getTarget(), databind.getField());
              }
            });
  }

  private Stream<Specification<Object>> joinsDefOnTarget(
      @NonNull Context context, @NonNull JoinContext jc, @NonNull Databind databind) {
    return Stream.concat(
            ofNullable(databind.getTarget().getClass().getAnnotation(Join.class)).stream(),
            ofNullable(databind.getTarget().getClass().getAnnotation(Joins.class)).stream()
                .flatMap(def -> stream(def.value())))
        .filter(def -> !jc.hasHandled(def, databind.getTarget(), null))
        .map(
            def -> {
              try {
                return newJoin(context, def);
              } finally {
                jc.markHandled(def, databind.getTarget(), null);
              }
            });
  }

  private Specification<Object> newJoin(@NonNull Context context, @NonNull Join def) {
    return new tw.com.softleader.data.jpa.spec.domain.Join<>(
        context, def.path(), def.alias(), def.joinType(), def.distinct());
  }

  @Override
  public void preVisit(@lombok.NonNull SpecInvocation node) {
    // 這隻不印
  }

  @Override
  public void postVisit(@lombok.NonNull SpecInvocation node, Specification<Object> resolved) {
    // 這隻不印
  }
}
