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

import java.util.Collection;
import java.util.StringJoiner;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
@RequiredArgsConstructor
abstract class CompoundSpecification<T> implements Specification<T> {

  @NonNull
  protected final transient Collection<Specification<T>> specs;

  @Override
  public Predicate toPredicate(Root root,
      CriteriaQuery query,
      CriteriaBuilder builder) {
    return specs.stream().reduce(this::combine)
        .map(spec -> spec.toPredicate(root, query, builder))
        .orElse(null);
  }

  /**
   * @param result 到目前 Combine 的結果
   * @param element 下一個元素
   */
  protected abstract Specification<T> combine(
      Specification<T> result,
      Specification<T> element);

  @Override
  public String toString() {
    return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
        .add("specs=" + specs)
        .toString();
  }
}
