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

import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import tw.com.softleader.data.jpa.spec.domain.Context;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PACKAGE;

/**
 * @author Matt Ho
 */
public interface SpecificationResolver extends ASTNode {

  boolean supports(@NonNull Databind databind);

  @Nullable
  Specification<Object> buildSpecification(@NonNull Context context, @NonNull Databind databind);

  static SpecificationResolverBuilder builder() {
    return new SpecificationResolverBuilder();
  }

  @NoArgsConstructor(access = PACKAGE)
  class SpecificationResolverBuilder {

    Predicate<Databind> supports;
    BiFunction<Context, Databind, Specification<Object>> buildSpecification;

    public SpecificationResolverBuilder supports(@NonNull Predicate<Databind> supports) {
      this.supports = supports;
      return this;
    }

    public SpecificationResolverBuilder buildSpecification(
        @NonNull BiFunction<Context, Databind, Specification<Object>> buildSpecification) {
      this.buildSpecification = buildSpecification;
      return this;
    }

    public SpecificationResolver build() {
      requireNonNull(supports, "'supports' must not be null");
      requireNonNull(buildSpecification, "'buildSpecification' must not be null");
      return new SpecificationResolver() {

        @Override
        public boolean supports(@lombok.NonNull Databind databind) {
          return supports.test(databind);
        }

        @Override
        public Specification<Object> buildSpecification(
            @NonNull Context context, @NonNull Databind databind) {
          return buildSpecification.apply(context, databind);
        }
      };
    }
  }

}
