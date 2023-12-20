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
import lombok.NonNull;

/**
 * {@code ... where x.age between ? and ?}
 *
 * @author Matt Ho
 */
public class Between<T> extends SimpleSpecification<T> {

  public Between(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path, value);
    if (!(value instanceof Iterable)) {
      throw new TypeMismatchException(value, Iterable.class);
    }
    if (stream(((Iterable<?>) value).spliterator(), false).count() != 2) {
      throw new IllegalArgumentException("@Between expected exact 2 elements, but was " + value);
    }
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    var args =
        stream(((Iterable<?>) value).spliterator(), false)
            .map(arg -> (Comparable<?>) arg)
            .toArray(Comparable[]::new);
    return builder.between(getPath(root), args[0], args[1]);
  }
}
