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

import static java.util.Collections.synchronizedMap;
import static java.util.Optional.ofNullable;

import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import lombok.NonNull;
import org.springframework.lang.Nullable;
import tw.com.softleader.data.jpa.spec.domain.JoinContext;

/**
 * @author Matt Ho
 */
class SpecJoinContext implements JoinContext {

  private final Map<HandleKey, Object> handled = synchronizedMap(new HashMap<>());

  /*
   * Join and fetch bookkeeping belongs to the single query execution that created it: a reused
   * Specification is executed against a brand new Root every time, so entries kept per Root would
   * pile up for as long as that Specification lives.
   *
   * The Root is therefore a weak key, which lets an entry die together with the criteria tree it
   * describes. The criteria nodes kept as values refer back to their Root, so they are held weakly
   * as well - a strong value would keep its own key reachable and defeat the weak key entirely
   * (see the WeakHashMap javadoc). That is safe because a Root owns every join and fetch built
   * from it, which keeps the referents alive for as long as the execution can still reach them.
   */
  private final Map<Root<?>, Map<String, WeakReference<Join<?, ?>>>> joined =
      synchronizedMap(new WeakHashMap<>());

  private final Map<Root<?>, Map<String, FetchEntry>> fetched =
      synchronizedMap(new WeakHashMap<>());

  @Override
  public boolean hasHandled(
      @NonNull Object target, @Nullable Object field, @NonNull Annotation def) {
    return handled.containsKey(new HandleKey(target, field, def));
  }

  @Override
  public void markHandled(@NonNull Object target, @Nullable Object field, @NonNull Annotation def) {
    handled.put(new HandleKey(target, field, def), null);
  }

  @Override
  public void putIfAbsent(@NonNull Root<?> root, @NonNull String alias, @NonNull Join<?, ?> join) {
    byAlias(joined, root).putIfAbsent(alias, new WeakReference<>(join));
  }

  @Override
  public void putIfAbsent(@NonNull Root<?> root, @NonNull String alias, @NonNull FetchRef ref) {
    byAlias(fetched, root)
        .putIfAbsent(alias, new FetchEntry(new WeakReference<>(ref.fetch()), ref.paths()));
  }

  @Override
  public Join<?, ?> getJoin(@NonNull Root<?> root, @NonNull String alias) {
    return ofNullable(joined.get(root))
        .map(aliases -> aliases.get(alias))
        .map(WeakReference::get)
        .orElse(null);
  }

  @Override
  public FetchRef getFetch(@NonNull Root<?> root, @NonNull String alias) {
    return ofNullable(fetched.get(root))
        .map(aliases -> aliases.get(alias))
        .map(FetchEntry::toRef)
        .orElse(null);
  }

  private static <V> Map<String, V> byAlias(
      @NonNull Map<Root<?>, Map<String, V>> byRoot, @NonNull Root<?> root) {
    return byRoot.computeIfAbsent(root, key -> synchronizedMap(new HashMap<>()));
  }

  record HandleKey(@NonNull String target, @Nullable String field, @NonNull Annotation def) {
    HandleKey(@NonNull Object target, @Nullable Object field, @NonNull Annotation def) {
      this(identityHex(target), identityHex(field), def);
    }

    static String identityHex(@Nullable Object obj) {
      return ofNullable(obj).map(System::identityHashCode).map(Integer::toHexString).orElse(null);
    }
  }

  record FetchEntry(@NonNull WeakReference<Fetch<?, ?>> fetch, @NonNull String[] paths) {

    @Nullable
    FetchRef toRef() {
      return ofNullable(fetch.get()).map(f -> new FetchRef(f, paths)).orElse(null);
    }
  }
}
