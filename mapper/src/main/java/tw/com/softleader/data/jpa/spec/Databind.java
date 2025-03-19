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

import java.lang.annotation.Annotation;
import java.util.Optional;
import org.springframework.lang.NonNull;

/**
 * An abstraction of a field within an object, allowing different strategies for retrieving field
 * values. This enables the possibility of replacing reflection-based access with more efficient
 * implementations.
 *
 * @author Matt Ho
 */
public interface Databind {

  /** Returns the target object containing the field. */
  @NonNull
  Object getTarget();

  /** Returns the field. */
  @NonNull
  FieldDescriptor getField();

  /**
   * Returns the value of the field.
   *
   * @return an {@code Optional} containing the field value, or empty if not accessible
   */
  Optional<Object> getFieldValue();

  /**
   * Checks whether the given annotation is present only on the target object and not on the field.
   *
   * <p>This method returns {@code true} if:
   *
   * <ul>
   *   <li>The field is absent (i.e., {@link FieldDescriptor#isAbsent()} returns {@code true}).
   *   <li>The target object's class is annotated with the specified annotation.
   * </ul>
   *
   * @param annotationClass the annotation type to check
   * @return {@code true} if the annotation is present on the target object but not on the field;
   *     {@code false} otherwise
   */
  default boolean isAnnotationPresentOnTargetOnly(
      @NonNull Class<? extends Annotation> annotationClass) {
    return getField().isAbsent() && getTarget().getClass().isAnnotationPresent(annotationClass);
  }
}
