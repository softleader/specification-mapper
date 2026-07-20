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

import static java.util.stream.StreamSupport.stream;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

/**
 * A {@link Specification} that generates a SQL {@code IN} clause in the query.
 *
 * <p>In Criteria API, an equivalent expression might be:
 *
 * <pre>{@code
 * root.get(path).in(values);
 * }</pre>
 *
 * <p>This typically translates to SQL like:
 *
 * <pre>{@code
 * ... WHERE x.firstname IN (?, ?, ...)
 * }</pre>
 *
 * <p>Collections larger than {@link #MAX_CHUNK_SIZE} are partitioned into OR-combined {@code IN}
 * clauses.
 *
 * @author Matt Ho
 * @see NotIn
 */
public class In<T> extends SimpleSpecification<T> {

  /**
   * The maximum number of elements expanded into a single {@code IN} clause.
   *
   * <p>Several RDBMS cap the number of elements of an {@code IN} clause, commonly at 1000, and huge
   * lists degrade the query plan; tune this to the target RDBMS if needed.
   */
  public static final int MAX_CHUNK_SIZE = 1000;

  public In(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path, value);
    if (!(value instanceof Iterable)) {
      throw new TypeMismatchException(value, Iterable.class);
    }
  }

  @Override
  public Predicate toPredicate(
      @NonNull Root<T> root, @Nullable CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
    var path = getPath(root);
    var values = stream(((Iterable<?>) value).spliterator(), false).toArray(Object[]::new);
    if (values.length <= MAX_CHUNK_SIZE) {
      return path.in(values);
    }
    var chunks = new ArrayList<Predicate>();
    for (var from = 0; from < values.length; from += MAX_CHUNK_SIZE) {
      var to = Math.min(from + MAX_CHUNK_SIZE, values.length);
      chunks.add(path.in(Arrays.copyOfRange(values, from, to)));
    }
    return builder.or(chunks.toArray(Predicate[]::new));
  }
}
