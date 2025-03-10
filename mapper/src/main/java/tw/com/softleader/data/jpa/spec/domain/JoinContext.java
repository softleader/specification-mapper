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
package tw.com.softleader.data.jpa.spec.domain;

import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Share data between join specifications
 *
 * @author Matt Ho
 */
public interface JoinContext {

  /**
   * The context key for storing {@code JoinContext} instance.
   *
   * @see Context
   */
  String CTX_JOIN = "_JOIN";

  /**
   * Checks whether the given target object and field have already been processed for the specified
   * annotation.
   *
   * @return {@code true} if the target and field have already been handled, otherwise {@code false}
   */
  boolean hasHandled(@NonNull Object target, @Nullable Field field, @NonNull Annotation def);

  /** Marks the given target object and field as processed for the specified annotation. */
  void markHandled(@NonNull Object target, @Nullable Field field, @NonNull Annotation def);

  /** Stores a {@link Join} object in the context if it is not already present. */
  void putIfAbsent(@NonNull Root<?> root, @NonNull String alias, @NonNull Join<?, ?> join);

  /** Stores a {@link FetchRef} object in the context if it is not already present. */
  void putIfAbsent(@NonNull Root<?> root, @NonNull String alias, @NonNull FetchRef ref);

  /**
   * Retrieves a {@link Join} associated with the given root entity and alias.
   *
   * @return the associated {@link Join}, or {@code null} if not found
   */
  @Nullable
  Join<?, ?> getJoin(@NonNull Root<?> root, @NonNull String alias);

  /**
   * Retrieves a {@link FetchRef} associated with the given root entity and alias.
   *
   * @return the associated {@link FetchRef}, or {@code null} if not found
   */
  @Nullable
  FetchRef getFetch(@NonNull Root<?> root, @NonNull String alias);

  /**
   * A record representing a fetch operation along with its associated paths.
   *
   * @param fetch the {@link Fetch} object, must not be {@code null}
   * @param paths the property paths related to the fetch, must not be {@code null}
   */
  record FetchRef(@NonNull Fetch<?, ?> fetch, @NonNull String... paths) {}
}
