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
import tw.com.softleader.data.jpa.spec.annotation.JoinFetch;
import tw.com.softleader.data.jpa.spec.annotation.JoinFetch.JoinFetches;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.JoinContext;

/**
 * A {@link SpecificationResolver} that processes fields annotated with {@link JoinFetch} or {@link
 * JoinFetches}.
 *
 * @author Matt Ho
 */
@Slf4j
class JoinFetchSpecificationResolver implements SpecificationResolver {

  @Override
  public boolean supports(@NonNull Databind databind) {
    return isAnnotationPresentOnFieldOrTargetClass(databind, JoinFetch.class)
        || isAnnotationPresentOnFieldOrTargetClass(databind, JoinFetches.class);
  }

  private boolean isAnnotationPresentOnFieldOrTargetClass(
      @NonNull Databind databind, Class<? extends Annotation> annotation) {
    return databind.getField().isAnnotationPresent(annotation)
        || databind.isAnnotationPresentOnTargetOnly(annotation);
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
            ofNullable(databind.getField().getAnnotation(JoinFetch.class)).stream(),
            ofNullable(databind.getField().getAnnotation(JoinFetches.class)).stream()
                .flatMap(def -> stream(def.value())))
        .filter(def -> !jc.hasHandled(databind.getTarget(), databind.getField(), def))
        .map(
            def -> {
              try {
                return newFetch(context, def);
              } finally {
                jc.markHandled(databind.getTarget(), databind.getField(), def);
              }
            });
  }

  private Stream<Specification<Object>> joinsDefOnTarget(
      @NonNull Context context, @NonNull JoinContext jc, @NonNull Databind databind) {
    return Stream.concat(
            ofNullable(databind.getTarget().getClass().getAnnotation(JoinFetch.class)).stream(),
            ofNullable(databind.getTarget().getClass().getAnnotation(JoinFetches.class)).stream()
                .flatMap(def -> stream(def.value())))
        .filter(def -> !jc.hasHandled(databind.getTarget(), null, def))
        .map(
            def -> {
              try {
                return newFetch(context, def);
              } finally {
                jc.markHandled(databind.getTarget(), null, def);
              }
            });
  }

  private Specification<Object> newFetch(@NonNull Context context, @NonNull JoinFetch def) {
    return new tw.com.softleader.data.jpa.spec.domain.JoinFetch<>(
        context, def.path(), def.alias(), def.joinType(), def.distinct());
  }

  @Override
  public void preVisit(@lombok.NonNull SpecInvocation node) {
    // 這邊不印
  }

  @Override
  public void postVisit(@lombok.NonNull SpecInvocation node, Specification<Object> resolved) {
    if (resolved == null) {
      return;
    }
    node.getAst()
        .add(
            node.getDepth(),
            "|    [%s.%s]: %s",
            node.getTargetType().getSimpleName(),
            node.getFieldName(),
            resolved);
  }
}
