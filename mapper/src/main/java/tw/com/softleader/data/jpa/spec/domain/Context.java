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

import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.NonNull;

/**
 * Share data between specifications
 *
 * @author Matt Ho
 */
public interface Context {

  /**
   * Returns the number of key-value pairs stored in the context.
   *
   * @return the number of entries in the context
   */
  int size();

  /**
   * Checks if the context is empty.
   *
   * @return {@code true} if the context contains no entries, otherwise {@code false}
   */
  boolean isEmpty();

  /** Removes all key-value pairs from the context. */
  void clear();

  /**
   * Checks whether the context contains the specified key.
   *
   * @param key the key whose presence is to be tested
   * @return {@code true} if the context contains the specified key, otherwise {@code false}
   */
  boolean containsKey(Object key);

  /**
   * Checks whether the context contains the specified value.
   *
   * @param value the value whose presence is to be tested
   * @return {@code true} if the context contains the specified value, otherwise {@code false}
   */
  boolean containsValue(Object value);

  /**
   * Retrieves the value associated with the given key.
   *
   * @param key the key whose associated value is to be retrieved
   * @return an {@code Optional} containing the value if present, otherwise an empty {@code
   *     Optional}
   */
  Optional<Object> get(Object key);

  /**
   * Retrieves the value associated with the given key from the Context and attempts to cast it to
   * the specified type.
   *
   * @param key the key whose associated value is to be retrieved
   * @param type the expected class type of the value
   * @param <T> the type of the value to be returned
   * @return the value cast to the specified type if present and compatible
   * @throws NoSuchElementException if the key is not found or the value cannot be cast to the
   *     specified type
   */
  default <T> T getAs(@NonNull Object key, @NonNull Class<T> type) {
    return get(key)
        .filter(type::isInstance)
        .map(type::cast)
        .orElseThrow(
            () ->
                new NoSuchElementException(
                    "No value found for key '%s', or value is not of expected type: %s"
                        .formatted(key, type.getSimpleName())));
  }

  /**
   * Associates the specified value with the specified key in the context. If the key already
   * exists, the previous value is replaced.
   *
   * @param key the key with which the specified value is to be associated
   * @param value the value to be stored
   * @return the previous value associated with the key, or {@code null} if there was no mapping for
   *     the key
   */
  Object put(Object key, Object value);

  /**
   * Removes the mapping for the specified key from the context if present.
   *
   * @param key the key whose mapping is to be removed
   * @return the previous value associated with the key, or {@code null} if there was no mapping for
   *     the key
   */
  Object remove(Object key);
}
