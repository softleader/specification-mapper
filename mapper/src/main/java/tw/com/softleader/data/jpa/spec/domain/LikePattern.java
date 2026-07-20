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

import java.util.Objects;
import lombok.NonNull;

/**
 * Composes the {@code LIKE} patterns used by {@link Like}, {@link NotLike}, {@link StartingWith}
 * and {@link EndingWith}.
 *
 * <p>User supplied values are matched literally: the wildcards {@code %} and {@code _}, as well as
 * the {@link #ESCAPE_CHAR escape character} itself, are escaped before being composed into a
 * pattern.
 *
 * @author Matt Ho
 */
public final class LikePattern {

  /** The escape character declared by every {@code LIKE} predicate of this package. */
  public static final char ESCAPE_CHAR = '\\';

  private LikePattern() {}

  /**
   * Escapes the {@code LIKE} wildcards of the given value, so that it is matched literally.
   *
   * @param value the value to escape
   * @return the escaped value
   */
  public static String escape(@NonNull Object value) {
    var text = Objects.toString(value);
    var escaped = new StringBuilder(text.length());
    for (var i = 0; i < text.length(); i++) {
      var c = text.charAt(i);
      if (c == ESCAPE_CHAR || c == '%' || c == '_') {
        escaped.append(ESCAPE_CHAR);
      }
      escaped.append(c);
    }
    return escaped.toString();
  }
}
