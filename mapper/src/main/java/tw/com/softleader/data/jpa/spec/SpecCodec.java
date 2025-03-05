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

import static java.util.Optional.ofNullable;

import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import tw.com.softleader.data.jpa.spec.domain.Context;

/**
 * A codec interface for mapping objects to JPA {@link Specification} instances.
 *
 * <p>Implementations provide mechanisms to convert domain objects into query specifications.
 *
 * @author Matt Ho
 */
public interface SpecCodec {

  /**
   * Converts the given root object into a {@link Specification}.
   *
   * @param rootObject the root object to be mapped
   * @return a {@code Specification} instance, or {@code null} if no mapping was found
   */
  @Nullable
  Specification<Object> toSpec(@Nullable Object rootObject);

  /**
   * Converts the given root object into a typed {@link Specification}.
   *
   * @param rootObject the root object to be mapped
   * @param rootType the target type of the specification
   * @param <T> the type parameter for the specification
   * @return a typed {@code Specification} instance, or {@code null} if no mapping was found
   */
  @Nullable
  @SuppressWarnings("unchecked")
  default <T> Specification<T> toSpec(@Nullable Object rootObject, @Nullable Class<T> rootType) {
    return (Specification<T>) toSpec(rootObject);
  }

  /**
   * Attempts to map the given root object into a {@link Specification}, returning an {@code
   * Optional}.
   *
   * @param rootObject the root object to be mapped
   * @return an {@code Optional} containing the mapped {@code Specification}, or empty if no mapping
   *     was found
   */
  @NonNull
  default Optional<Specification<Object>> trySpec(@Nullable Object rootObject) {
    return ofNullable(toSpec(rootObject));
  }

  /**
   * Attempts to map the given root object into a typed {@link Specification}, returning an {@code
   * Optional}.
   *
   * @param rootObject the root object to be mapped
   * @param rootType the target type of the specification
   * @param <T> the type parameter for the specification
   * @return an {@code Optional} containing the mapped {@code Specification}, or empty if no mapping
   *     was found
   */
  @NonNull
  default <T> Optional<Specification<T>> trySpec(
      @Nullable T rootObject, @Nullable Class<T> rootType) {
    return ofNullable(toSpec(rootObject, rootType));
  }

  /**
   * Attempts to map the given root object into a {@link Specification} using the provided context.
   *
   * @param context the processing context
   * @param rootObject the root object to be mapped
   * @return an {@code Optional} containing the mapped {@code Specification}, or empty if no mapping
   *     was found
   */
  @NonNull
  default Optional<Specification<Object>> trySpec(
      @NonNull Context context, @Nullable Object rootObject) {
    return ofNullable(toSpec(context, rootObject));
  }

  /**
   * Attempts to map the given root object into a typed {@link Specification} using the provided
   * context.
   *
   * @param context the processing context
   * @param rootObject the root object to be mapped
   * @param rootType the target type of the specification
   * @param <T> the type parameter for the specification
   * @return an {@code Optional} containing the mapped {@code Specification}, or empty if no mapping
   *     was found
   */
  @NonNull
  default <T> Optional<Specification<T>> trySpec(
      @NonNull Context context, @Nullable T rootObject, @Nullable Class<T> rootType) {
    return ofNullable(toSpec(context, rootObject, rootType));
  }

  /**
   * Converts the given root object into a {@link Specification} using the provided context.
   *
   * @param context the processing context
   * @param rootObject the root object to be mapped
   * @return a {@code Specification} instance, or {@code null} if no mapping was found
   */
  @Nullable
  Specification<Object> toSpec(@NonNull Context context, @Nullable Object rootObject);

  /**
   * Converts the given root object into a typed {@link Specification} using the provided context.
   *
   * @param context the processing context
   * @param rootObject the root object to be mapped
   * @param rootType the target type of the specification
   * @param <T> the type parameter for the specification
   * @return a typed {@code Specification} instance, or {@code null} if no mapping was found
   */
  @Nullable
  @SuppressWarnings("unchecked")
  default <T> Specification<T> toSpec(
      @NonNull Context context, @Nullable Object rootObject, @Nullable Class<T> rootType) {
    return (Specification<T>) toSpec(context, rootObject);
  }
}
