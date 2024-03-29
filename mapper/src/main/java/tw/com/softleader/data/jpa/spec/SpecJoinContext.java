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
package tw.com.softleader.data.jpa.spec;

import static java.util.Collections.synchronizedMap;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.Synchronized;
import org.springframework.data.util.Pair;
import tw.com.softleader.data.jpa.spec.domain.JoinContext;

/**
 * @author Matt Ho
 */
class SpecJoinContext implements JoinContext {

  private final Map<Pair<String, Root<?>>, Join<?, ?>> joins = synchronizedMap(new HashMap<>());
  private final Map<String, Function<Root<?>, Join<?, ?>>> lazyJoins =
      synchronizedMap(new HashMap<>());

  @Override
  @Synchronized
  public Join<?, ?> get(String key, Root<?> root) {
    var lazyJoin = lazyJoins.get(key);
    if (lazyJoin == null) {
      return null;
    }
    Pair<String, Root<?>> rootKey = Pair.of(key, root);
    joins.computeIfAbsent(rootKey, k -> lazyJoin.apply(root));
    return joins.get(rootKey);
  }

  @Override
  public void putLazy(String key, Function<Root<?>, Join<?, ?>> lazyJoin) {
    lazyJoins.put(key, lazyJoin);
  }
}
