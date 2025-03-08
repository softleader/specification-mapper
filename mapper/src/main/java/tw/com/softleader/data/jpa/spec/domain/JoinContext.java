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

  String CTX_JOIN = "_JOIN";

  boolean hasHandled(@NonNull Object target, @Nullable Field field, @NonNull Annotation def);

  void markHandled(@NonNull Object target, @Nullable Field field, @NonNull Annotation def);

  void putIfAbsent(@NonNull Root<?> root, @NonNull String alias, @NonNull Join<?, ?> join);

  void putIfAbsent(@NonNull Root<?> root, @NonNull String alias, @NonNull FetchRef ref);

  @Nullable
  Join<?, ?> getJoin(@NonNull Root<?> root, @NonNull String alias);

  @Nullable
  FetchRef getFetch(@NonNull Root<?> root, @NonNull String alias);

  record FetchRef(@NonNull Fetch<?, ?> fetch, @NonNull String... paths) {}
}
