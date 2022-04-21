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
 * @author Matt Ho
 */
public interface SpecCodec {

  /**
   * @return null if non any {@code Specification} was mapped
   */
  @SuppressWarnings("unchecked")
  @Nullable
  Specification<Object> toSpec(@Nullable Object rootObject);

  /**
   * @return null if non any {@code Specification} was mapped
   */
  @Nullable
  @SuppressWarnings("unchecked")
  default <T> Specification<T> toSpec(@Nullable Object rootObject, @Nullable Class<T> rootType) {
    return (Specification<T>) toSpec(rootObject);
  }

  /**
   * @return empty if non any {@code Specification} was mapped
   */
  @NonNull
  default Optional<Specification<Object>> trySpec(@Nullable Object rootObject) {
    return ofNullable(toSpec(rootObject));
  }

  /**
   * @return empty if non any {@code Specification} was mapped
   */
  @NonNull
  default <T> Optional<Specification<T>> trySpec(@Nullable T rootObject,
      @Nullable Class<T> rootType) {
    return ofNullable(toSpec(rootObject, rootType));
  }

  /**
   * @return empty if non any {@code Specification} was mapped
   */
  @NonNull
  default Optional<Specification<Object>> trySpec(@NonNull Context context,
      @Nullable Object rootObject) {
    return ofNullable(toSpec(context, rootObject));
  }

  /**
   * @return empty if non any {@code Specification} was mapped
   */
  @NonNull
  default <T> Optional<Specification<T>> trySpec(@NonNull Context context, @Nullable T rootObject,
      @Nullable Class<T> rootType) {
    return ofNullable(toSpec(context, rootObject, rootType));
  }

  /**
   * @return null if non any {@code Specification} was mapped
   */
  @Nullable
  Specification<Object> toSpec(@NonNull Context context, @Nullable Object rootObject);

  /**
   * @return null if non any {@code Specification} was mapped
   */
  @Nullable
  @SuppressWarnings("unchecked")
  default <T> Specification<T> toSpec(@NonNull Context context, @Nullable Object rootObject,
      @Nullable Class<T> rootType) {
    return (Specification<T>) toSpec(context, rootObject);
  }
}
