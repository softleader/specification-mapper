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

import static java.util.Optional.ofNullable;
import static org.springframework.util.ReflectionUtils.accessibleConstructor;
import static tw.com.softleader.data.jpa.spec.domain.JoinContext.CTX_JOIN;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.StringJoiner;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.domain.JoinContext.FetchRef;

/**
 * Abstract class representing a simple specification used for querying or filtering data.
 *
 * <p>The implementations must provide accessible constructor.
 *
 * @param <T> The type of the entity this specification applies to
 * @author Matt Ho
 */
public abstract class SimpleSpecification<T> implements Specification<T> {

  protected final transient Context context;
  protected final String path;
  protected final transient Object value;

  protected SimpleSpecification(
      @NonNull Context context, @NonNull String path, @NonNull Object value) {
    this.context = context;
    this.path = path;
    this.value = value;
  }

  @Builder
  @SneakyThrows
  @SuppressWarnings({"unchecked", "rawtypes"})
  private static <T> Specification<T> newSpec(
      @NonNull Context context,
      @NonNull Class<? extends SimpleSpecification> domainClass,
      @NonNull String path,
      @NonNull Object value) {
    try {
      return accessibleConstructor(domainClass, Context.class, String.class, Object.class)
          .newInstance(context, path, value);
    } catch (InvocationTargetException e) {
      // Constructor.newInstance wraps any exception thrown inside the constructor in an
      // InvocationTargetException; surface the original cause (e.g. TypeMismatchException,
      // IllegalArgumentException) so it reaches SpecMapper.toSpec callers.
      var cause = e.getCause();
      if (cause instanceof RuntimeException runtimeException) {
        throw runtimeException;
      }
      if (cause instanceof Error error) {
        throw error;
      }
      throw new IllegalStateException(cause);
    }
  }

  @SuppressWarnings({"unchecked"})
  protected <F> Path<F> getPath(@NonNull Root<T> root) {
    var split = path.split("\\.");
    // 處理單一層的 path
    if (split.length == 1) {
      return root.get(split[0]);
    }
    // 處理多層的 path
    var expr = getExpr(root, split[0]);
    for (int i = 1; i < split.length; i++) {
      expr = expr.get(split[i]);
    }
    return (Path<F>) expr;
  }

  private Path<?> getExpr(@NonNull Root<T> root, @NonNull String field) {
    return getJoin(root, field).or(() -> getFetch(root, field)).orElseGet(() -> root.get(field));
  }

  @SuppressWarnings({"unchecked"})
  private Optional<Path<T>> getJoin(@NonNull Root<T> root, @NonNull String field) {
    return ofNullable(context.getAs(CTX_JOIN, JoinContext.class).getJoin(root, field))
        .map(joined -> (Path<T>) joined);
  }

  private Optional<Path<T>> getFetch(@NonNull Root<T> root, @NonNull String field) {
    return ofNullable(context.getAs(CTX_JOIN, JoinContext.class).getFetch(root, field))
        .map(ref -> getFetchPath(root, ref));
  }

  private Path<T> getFetchPath(@NonNull Root<T> root, @NonNull FetchRef ref) {
    Path<T> current = root;
    for (var path : ref.paths()) {
      current = current.get(path);
    }
    return current;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
        .add("path=" + path)
        .add("value=" + value)
        .toString();
  }
}
