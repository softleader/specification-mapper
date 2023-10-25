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

import static tw.com.softleader.data.jpa.spec.AST.CTX_AST;
import static tw.com.softleader.data.jpa.spec.AST.CTX_DEPTH;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import tw.com.softleader.data.jpa.spec.annotation.And;
import tw.com.softleader.data.jpa.spec.annotation.Or;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.Not;
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
    var ast = context.get(CTX_AST).map(AST.class::cast).get();
    var depth = (int) context.get(CTX_DEPTH).get();
    var built = databind.getFieldValue()
        .map(value -> buildSpecification(context, databind, def, value))
        .orElse(null);
    ast.add(depth, "|  +-[%s.%s]: @Spec(value=%s, path=%s, not=%s) -> %s",
        databind.getTarget().getClass().getSimpleName(),
        databind.getField().getName(),
        def.value().getSimpleName(),
        def.path(),
        def.not(),
        built);
    return built;
  }

  private Specification<Object> buildSpecification(@NonNull Context context,
      @NonNull Databind databind, @NonNull Spec def,
      Object value) {
    var path = of(def.path())
        .filter(StringUtils::hasText)
        .orElseGet(databind.getField()::getName);
    Specification<Object> spec = SimpleSpecification.builder()
        .context(context)
        .domainClass(def.value())
        .path(path)
        .value(value)
        .build();
    if (def.not()) {
      spec = new Not<>(spec);
    }
    if (databind.getField().isAnnotationPresent(And.class)) {
      return new tw.com.softleader.data.jpa.spec.domain.And<>(spec);
    }
    if (databind.getField().isAnnotationPresent(Or.class)) {
      return new tw.com.softleader.data.jpa.spec.domain.Or<>(spec);
    }
    return spec;
  }

  @Override
  public void preVisit(@lombok.NonNull SpecInvocation node) {
    // 這隻自己處理 AST
  }

  @Override
  public void postVisit(@lombok.NonNull SpecInvocation node, Specification<Object> resolved) {
    // 這隻自己處理 AST
  }
}
