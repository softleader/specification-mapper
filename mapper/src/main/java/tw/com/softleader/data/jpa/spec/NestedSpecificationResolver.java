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

import static tw.com.softleader.data.jpa.spec.AST.CTX_DEPTH;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    return databind.getFieldValue()
        .map(nested -> {
          var depth = (int) context.get(CTX_DEPTH).get();
          context.put(CTX_DEPTH, depth + 1);
          var spec = codec.toSpec(context, nested);
          if (spec == null) {
            return null;
          }
          var field = databind.getField();
          if (field.isAnnotationPresent(And.class)) {
            return new tw.com.softleader.data.jpa.spec.domain.And<>(spec);
          }
          if (field.isAnnotationPresent(Or.class)) {
            return new tw.com.softleader.data.jpa.spec.domain.Or<>(spec);
          }
          return spec;
        })
        .orElse(null);
  }
}
