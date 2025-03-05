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

import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PACKAGE;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import lombok.NoArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import tw.com.softleader.data.jpa.spec.domain.Context;

/**
 * A resolver that processes {@link Databind} and constructs the corresponding {@link
 * Specification}.
 *
 * <p>Implementations of this interface determine whether they support a given databind instance and
 * generate the appropriate specification accordingly.
 *
 * <p>Implementations can define the order in which they should be applied by implementing the
 * {@link Ordered} interface.
 *
 * @author Matt Ho
 */
public interface SpecificationResolver extends ASTNode, Ordered {

  /**
   * Determines whether this resolver supports the given {@link Databind}.
   *
   * @param databind the target databind object
   * @return {@code true} if this resolver supports the given databind, {@code false} otherwise
   */
  boolean supports(@NonNull Databind databind);

  /**
   * Builds the corresponding {@link Specification}, or returns {@code null} if it cannot be
   * constructed.
   *
   * @param context the current processing context
   * @param databind the target databind object
   * @return the constructed {@link Specification}, or {@code null} if not applicable
   */
  @Nullable
  Specification<Object> buildSpecification(@NonNull Context context, @NonNull Databind databind);

  /**
   * Gets the order of this resolver. Lower values indicate higher priority.
   *
   * @return the order value of this resolver
   */
  default int getOrder() {
    return 0;
  }

  /**
   * Creates a new {@link SpecificationResolverBuilder} for constructing instances of {@link
   * SpecificationResolver}.
   *
   * @return a new {@link SpecificationResolverBuilder} instance
   */
  static SpecificationResolverBuilder builder() {
    return new SpecificationResolverBuilder();
  }

  /**
   * A builder for constructing instances of {@link SpecificationResolver}.
   *
   * <p>This builder allows setting the conditions for supporting a databind, the function for
   * building specifications, and the order of the resolver.
   */
  @NoArgsConstructor(access = PACKAGE)
  class SpecificationResolverBuilder implements Ordered {

    Predicate<Databind> supports;
    BiFunction<Context, Databind, Specification<Object>> buildSpecification;
    int order = 0;

    /**
     * Specifies the predicate used to determine if the resolver supports a given {@link Databind}.
     *
     * @param supports a predicate that tests whether the databind is supported
     * @return this builder instance for method chaining
     */
    public SpecificationResolverBuilder supports(@NonNull Predicate<Databind> supports) {
      this.supports = supports;
      return this;
    }

    /**
     * Defines the function used to build the corresponding {@link Specification}.
     *
     * @param buildSpecification a function that constructs the specification from the given context
     *     and databind
     * @return this builder instance for method chaining
     */
    public SpecificationResolverBuilder buildSpecification(
        @NonNull BiFunction<Context, Databind, Specification<Object>> buildSpecification) {
      this.buildSpecification = buildSpecification;
      return this;
    }

    /**
     * Sets the order of this resolver.
     *
     * @param order the order value, where lower values indicate higher priority
     * @return this builder instance for method chaining
     */
    public SpecificationResolverBuilder order(int order) {
      this.order = order;
      return this;
    }

    /**
     * Builds and returns a new instance of {@link SpecificationResolver} based on the configured
     * parameters.
     *
     * @return a new {@link SpecificationResolver} instance
     * @throws NullPointerException if either the supports predicate or buildSpecification function
     *     is not set
     */
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

        @Override
        public int getOrder() {
          return order;
        }
      };
    }

    @Override
    public int getOrder() {
      return 0;
    }
  }
}
