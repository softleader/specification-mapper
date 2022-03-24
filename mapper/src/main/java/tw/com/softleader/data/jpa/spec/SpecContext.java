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
import static java.util.Optional.ofNullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.JoinContext;

class SpecContext implements Context {

  private final Map<Object, Object> bag = synchronizedMap(new HashMap<>());
  private final JoinContext join = new SpecJoinContext();

  @Override
  public JoinContext join() {
    return join;
  }

  @Override
  public int size() {
    return bag.size();
  }

  @Override
  public boolean isEmpty() {
    return bag.isEmpty();
  }

  @Override
  public void clear() {
    bag.clear();
  }

  @Override
  public boolean containsKey(@NonNull Object key) {
    return bag.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return false;
  }

  @Override
  public Optional<Object> get(@NonNull Object key) {
    return ofNullable(bag.get(key));
  }

  @Override
  public Object put(@NonNull Object key, @NonNull Object value) {
    return bag.put(key, value);
  }

  @Override
  public Object remove(@NonNull Object key) {
    return bag.remove(key);
  }
}
