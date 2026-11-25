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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

/**
 * A {@link Specification} that generates a SQL not {@code IN} clause in the query.
 *
 * <p>In Criteria API, an equivalent expression might be:
 *
 * <pre>{@code
 * Predicate.not(root.get(path).in(values));
 * }</pre>
 *
 * <p>This typically translates to SQL like:
 *
 * <pre>{@code
 * ... where x.firstname not in (?, ?, ...)
 * }</pre>
 *
 * <p>Collections larger than {@link In#MAX_CHUNK_SIZE} are partitioned the same way {@link In}
 * does, negating the OR-combined chunks as a whole.
 *
 * @author Matt Ho
 * @see In
 */
public class NotIn<T> extends In<T> {

  public NotIn(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path, value);
  }

  @Override
  public Predicate toPredicate(
      @NonNull Root<T> root, @Nullable CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
    return ofNullable(super.toPredicate(root, query, builder)).map(Predicate::not).orElse(null);
  }
}
