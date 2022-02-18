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

import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;

/**
 * {@code ... where x.firstname not like %?%}
 *
 * @author Matt Ho
 */
public class NotLike<T> extends SimpleSpecification<T> {

  public NotLike(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path, "%" + value + "%");
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    return builder.notLike(getPath(root), Objects.toString(value));
  }
}
