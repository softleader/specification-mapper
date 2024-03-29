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

import lombok.Getter;
import lombok.NonNull;

/**
 * @author Matt Ho
 */
@Getter
class ReflectionSpecInvocation implements SpecInvocation {

  private final AST ast;
  private final int depth;
  private final Class<? extends SpecificationResolver> resolverType;
  private final Class<?> targetType;
  private final Class<?> fieldType;
  private final String fieldName;

  ReflectionSpecInvocation(
      @NonNull AST ast,
      int depth,
      @NonNull SpecificationResolver resolver,
      @NonNull Databind databind) {
    this.ast = ast;
    this.depth = depth;
    this.resolverType = resolver.getClass();
    this.targetType = databind.getTarget().getClass();
    this.fieldType = databind.getField().getType();
    this.fieldName = databind.getField().getName();
    if (depth < 0) {
      throw new IllegalArgumentException("depth must >= 0, but was " + depth);
    }
  }
}
