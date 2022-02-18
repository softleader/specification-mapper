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

import java.util.StringJoiner;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

/**
 * To constraint the constructor, the implementations must provide accessible constructor.
 *
 * @author Matt Ho
 */
public abstract class SimpleSpecification<T> implements Specification<T> {

  protected final transient Context context;
  protected final String path;
  protected final transient Object value;

  protected SimpleSpecification(@NonNull Context context, @NonNull String path,
      @NonNull Object value) {
    this.context = context;
    this.path = path;
    this.value = value;
  }

  protected <F> Path<F> getPath(Root<T> root) {
    var split = path.split("\\.");
    if (split.length == 1) {
      return root.get(split[0]);
    }
    Path<?> expr = null;
    for (String field : split) {
      if (expr == null) {
        expr = ofNullable(context.getJoin(field, root))
            .map(joined -> (Path<T>) joined)
            .orElseGet(() -> root.get(field));
        continue;
      }
      expr = expr.get(field);
    }
    return (Path<F>) expr;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
        .add("path='" + path + "'")
        .add("value=" + value)
        .toString();
  }
}
