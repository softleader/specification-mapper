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

import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * @author Matt Ho
 */
@Getter
public class TypeMismatchException extends RuntimeException {

  @Nullable private final transient Object value;

  @Nullable private final Class<?> requiredType;

  public TypeMismatchException(
      @Nullable Object value, @Nullable Class<?> requiredType, Class<?>... requiredTypes) {
    this(null, value, requiredType, requiredTypes);
  }

  public TypeMismatchException(
      @Nullable Throwable cause,
      @Nullable Object value,
      @Nullable Class<?> requiredType,
      Class<?>... requiredTypes) {
    super(
        "Failed to convert value of type '"
            + ClassUtils.getDescriptiveType(value)
            + "'"
            + (requiredType != null
                ? " to required type '"
                    + Stream.concat(Stream.of(requiredType), Stream.of(requiredTypes))
                        .map(ClassUtils::getQualifiedName)
                        .collect(joining(" or "))
                    + "'"
                : ""),
        cause);
    this.value = value;
    this.requiredType = requiredType;
  }
}
