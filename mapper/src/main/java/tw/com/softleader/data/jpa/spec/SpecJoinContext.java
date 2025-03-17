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

import static java.lang.Integer.toHexString;
import static java.lang.System.identityHashCode;
import static java.util.Collections.synchronizedMap;
import static java.util.Optional.ofNullable;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.springframework.lang.Nullable;
import tw.com.softleader.data.jpa.spec.domain.JoinContext;

/**
 * @author Matt Ho
 */
class SpecJoinContext implements JoinContext {

  private final Map<HandleKey, Object> handled = synchronizedMap(new HashMap<>());
  private final Map<JoinKey, Join<?, ?>> joined = synchronizedMap(new HashMap<>());
  private final Map<FetchKey, FetchRef> fetched = synchronizedMap(new HashMap<>());

  @Override
  public boolean hasHandled(
      @NonNull Object target, @Nullable Field field, @NonNull Annotation def) {
    return handled.containsKey(new HandleKey(target, field, def));
  }

  @Override
  public void markHandled(@NonNull Object target, @Nullable Field field, @NonNull Annotation def) {
    handled.put(new HandleKey(target, field, def), null);
  }

  @Override
  public void putIfAbsent(@NonNull Root<?> root, @NonNull String alias, @NonNull Join<?, ?> join) {
    joined.putIfAbsent(new JoinKey(root, alias), join);
  }

  @Override
  public void putIfAbsent(@NonNull Root<?> root, @NonNull String alias, @NonNull FetchRef ref) {
    fetched.putIfAbsent(new FetchKey(root, alias), ref);
  }

  @Override
  public Join<?, ?> getJoin(@NonNull Root<?> root, @NonNull String alias) {
    return joined.get(new JoinKey(root, alias));
  }

  @Override
  public FetchRef getFetch(@NonNull Root<?> root, @NonNull String alias) {
    return fetched.get(new FetchKey(root, alias));
  }

  record HandleKey(@NonNull String target, @Nullable String field, @NonNull Annotation def) {
    HandleKey(@NonNull Object target, @Nullable Field field, @NonNull Annotation def) {
      this(identityHex(target), ofNullable(field).map(HandleKey::identityHex).orElse(null), def);
    }

    static String identityHex(@NonNull Object obj) {
      return toHexString(identityHashCode(obj));
    }
  }

  record JoinKey(@NonNull Root<?> root, @NonNull String alias) {}

  record FetchKey(@NonNull Root<?> root, @NonNull String alias) {}
}
