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

import static java.util.Comparator.comparing;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.StringJoiner;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
@RequiredArgsConstructor
abstract class CompoundSpecification<T> implements Specification<T> {

  @NonNull protected final transient List<Specification<T>> specs;

  /**
   * Fold 的結果會相依於 element 的順序, 因此在 Fold 前會先把所有覆寫了預設運算子的 element 穩定排序到最後, 如此一來第一個 element
   * 就必定是使用預設運算子的, 也就不會有 Wrapper 被忽略的問題; 換句話說, 無論欄位的宣告順序為何, 都會得到相同的組合結果
   */
  @Override
  public Predicate toPredicate(
      @NonNull Root<T> root, CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
    return specs.stream()
        .sorted(comparing(this::overridesOperator))
        .reduce(this::combine)
        .map(spec -> spec.toPredicate(root, query, builder))
        .orElse(null);
  }

  /**
   * @param element 要檢查的元素
   * @return 該元素是否覆寫了本 Compound 的預設運算子
   */
  protected abstract boolean overridesOperator(Specification<T> element);

  /**
   * @param result 到目前 Combine 的結果
   * @param element 下一個元素
   */
  protected abstract Specification<T> combine(Specification<T> result, Specification<T> element);

  @Override
  public String toString() {
    return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
        .add("specs=" + specs)
        .toString();
  }
}
