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

import static tw.com.softleader.data.jpa.spec.AST.CTX_AST;
import static tw.com.softleader.data.jpa.spec.AST.CTX_DEPTH;

import java.lang.reflect.Field;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import tw.com.softleader.data.jpa.spec.annotation.And;
import tw.com.softleader.data.jpa.spec.annotation.NestedSpec;
import tw.com.softleader.data.jpa.spec.annotation.Or;
import tw.com.softleader.data.jpa.spec.domain.Context;

/**
 * @author Matt Ho
 */
@Slf4j
@RequiredArgsConstructor
class NestedSpecificationResolver implements SpecificationResolver {

  final SpecCodec codec;

  @Override
  public boolean supports(@NonNull Databind databind) {
    return databind.getField().isAnnotationPresent(NestedSpec.class);
  }

  @Override
  public Specification<Object> buildSpecification(@NonNull Context context,
      @NonNull Databind databind) {
    val ast = context.get(CTX_AST).map(AST.class::cast).get();
    val depth = (int) context.get(CTX_DEPTH).get();
    return databind.getFieldValue()
        .map(nested -> {
          ast.add(depth, "|  +-[%s.%s] (%s)",
              databind.getTarget().getClass().getSimpleName(),
              databind.getField().getName(),
              databind.getField().getType());
          context.put(CTX_DEPTH, depth + 1);
          val spec = codec.toSpec(context, nested);
          val compound = combine(databind.getField(), spec);
          ast.add(depth, "|  \\-[%s.%s]: %s",
              databind.getTarget().getClass().getSimpleName(),
              databind.getField().getName(),
              compound);
          return compound;
        })
        .orElse(null);
  }

  private Specification<Object> combine(@lombok.NonNull Field field, Specification<Object> spec) {
    if (field.isAnnotationPresent(And.class)) {
      return new tw.com.softleader.data.jpa.spec.domain.And<>(spec);
    }
    if (field.isAnnotationPresent(Or.class)) {
      return new tw.com.softleader.data.jpa.spec.domain.Or<>(spec);
    }
    return spec;
  }
}
