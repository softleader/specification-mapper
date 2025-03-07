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

import jakarta.persistence.criteria.*;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

/**
 * A {@code Specification} that performs an inner join and fetches the related entities.
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

  private final List<String> pathsToFetch;
  private final JoinType joinType;
  private final boolean distinct;

  public JoinFetch(@NonNull String[] pathsToFetch, @NonNull JoinType joinType, boolean distinct) {
    this.pathsToFetch = Arrays.asList(pathsToFetch);
    this.joinType = joinType;
    this.distinct = distinct;

    if (pathsToFetch.length == 0) {
      throw new IllegalArgumentException("paths must not be empty");
    }
  }

  @Override
  public Predicate toPredicate(
      @NonNull Root<T> root, @Nullable CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
    if (query != null) {
      query.distinct(distinct);
      if (Number.class.isAssignableFrom(query.getResultType())) { // do not join in count queries
        return null;
      }
    }
    fetchJoin(root);
    return null;
  }

  private void fetchJoin(Root<T> root) {
    if (pathsToFetch.size() > 1) {
      for (String path : pathsToFetch) {
        root.fetch(path, joinType);
      }
      return;
    }
    var pathToFetch = pathsToFetch.get(0);
    if (!pathToFetch.contains(".")) {
      root.fetch(pathToFetch, joinType);
      return;
    }
    var byDot = pathToFetch.split("\\.");
    var alias = byDot[0];
    var path = byDot[1];
    root.fetch(alias).fetch(path, joinType);
  }
}
