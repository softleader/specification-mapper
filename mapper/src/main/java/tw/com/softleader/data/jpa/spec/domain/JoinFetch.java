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
import static tw.com.softleader.data.jpa.spec.domain.JoinContext.CTX_JOIN;

import jakarta.persistence.criteria.*;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import tw.com.softleader.data.jpa.spec.domain.JoinContext.FetchRef;

/**
 * A {@link Specification} that performs an inner join and fetches the related entities.
 *
 * <p>In Criteria API, an equivalent expression might be:
 *
 * <pre>{@code
 * root.fetch(root.get(path), joinType)
 * }</pre>
 *
 * <p>This typically translates to SQL like:
 *
 * <pre>
 * {@code join x on x.id = y.id}
 * </pre>
 *
 * @author Matt Ho
 */
@ToString
public class JoinFetch<T> implements Specification<T> {

  @ToString.Exclude @NonNull private final transient Context context;
  @NonNull private final String pathToFetch;
  @NonNull private final String alias;
  @NonNull private final JoinType joinType;
  private final boolean distinct;

  public JoinFetch(
      @NonNull Context context,
      @NonNull String pathToFetch,
      @Nullable String alias,
      @NonNull JoinType joinType,
      boolean distinct) {
    this.context = context;
    this.pathToFetch = pathToFetch;
    this.alias =
        ofNullable(alias)
            .filter(StringUtils::hasText)
            .orElseGet(() -> pathToFetch.replace(".", "_"));
    this.joinType = joinType;
    this.distinct = distinct;
  }

  @Override
  public Predicate toPredicate(
      @NonNull Root<T> root, @Nullable CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
    if (query != null) {
      query.distinct(distinct);
      if (!isCountQuery(query)) { // do not join in count queries
        fetch(root);
      }
    }
    return null;
  }

  private boolean isCountQuery(@NonNull CriteriaQuery<?> query) {
    return Number.class.isAssignableFrom(query.getResultType());
  }

  private void fetch(Root<T> root) {
    var jc = context.getAs(CTX_JOIN, JoinContext.class);
    if (!pathToFetch.contains(".")) {
      jc.putIfAbsent(root, alias, new FetchRef(root.fetch(pathToFetch, joinType), pathToFetch));
      return;
    }
    var byDot = pathToFetch.split("\\.");

    var extractedAlias = byDot[0];
    var ref = jc.getFetch(root, extractedAlias);
    if (ref == null) {
      throw new IllegalArgumentException(
          "JoinFetch definition with alias: '%s' not found! Make sure that fetch with the alias '%s' is defined before the fetch with path: '%s'"
              .formatted(extractedAlias, extractedAlias, pathToFetch));
    }

    var extractedPathToFetch = byDot[1];
    jc.putIfAbsent(
        root, alias, new FetchRef(ref.fetch().fetch(extractedPathToFetch, joinType), byDot));
  }
}
