/*
 * Copyright © 2022 SoftLeader (support@softleader.com.tw)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tw.com.softleader.data.jpa.spec.domain;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
@RequiredArgsConstructor
public class Join<T> implements Specification<T> {

  @NonNull
  private final transient Context context;
  @NonNull
  private final String pathToJoinOn;
  @NonNull
  private final String alias;
  @NonNull
  private final JoinType joinType;
  private final boolean distinct;

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    query.distinct(distinct);
    join(root);
    return null;
  }

  private void join(Root<T> root) {
    if (!pathToJoinOn.contains(".")) {
      context.putLazyJoin(alias, r -> r.join(pathToJoinOn, joinType));
      return;
    }
    var byDot = pathToJoinOn.split("\\.");

    var extractedAlias = byDot[0];
    var joined = context.getJoin(extractedAlias, root);
    if (joined == null) {
      throw new IllegalArgumentException(
          "Join definition with alias: '" + extractedAlias + "' not found! " +
              "Make sure that join with the alias '" + extractedAlias
              + "' is defined before the join with path: '" + pathToJoinOn + "'");
    }

    var extractedPathToJoin = byDot[1];
    context.putLazyJoin(alias, r -> joined.join(extractedPathToJoin, joinType));
  }
}
