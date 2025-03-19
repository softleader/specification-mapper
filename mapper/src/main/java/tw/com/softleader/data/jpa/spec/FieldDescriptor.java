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
import java.lang.reflect.AnnotatedElement;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Represents a field descriptor that provides metadata about a field, including its name, type, and
 * associated annotations.
 *
 * <p>This interface extends {@link AnnotatedElement} to allow retrieval of field-level annotations.
 * Implementations of this interface may wrap actual fields or represent an absent field descriptor
 * using the singleton instance {@link #ABSENT}.
 *
 * <p>It also provides an {@link #unwrap(Class)} method to retrieve the underlying representation of
 * the field in a specific type.
 *
 * @author Matt Ho
 */
public interface FieldDescriptor extends AnnotatedElement {

  /**
   * A singleton instance representing an absent field descriptor. This instance is used when there
   * is no meaningful field representation.
   */
  FieldDescriptor ABSENT = new AbsentFieldDescriptor();

  /**
   * Returns the name of the field.
   *
   * @return the field name
   */
  @NonNull
  String getName();

  /**
   * Returns the type of the field.
   *
   * @return the field's class type
   */
  @NonNull
  Class<?> getType();

  /**
   * Checks whether this descriptor represents an absent field.
   *
   * @return {@code true} if this is an instance of {@link AbsentFieldDescriptor}, otherwise {@code
   *     false}
   */
  default boolean isAbsent() {
    return this instanceof AbsentFieldDescriptor;
  }

  /**
   * Unwraps the underlying instance as the specified type.
   *
   * <p>If {@link #unwrap()} returns {@code null}, this method also returns {@code null}. If the
   * unwrapped instance is not assignable to {@code iface}, a {@link ClassCastException} is thrown.
   *
   * @param iface the target type to unwrap to
   * @param <T> the expected type
   * @return the unwrapped instance cast to {@code iface}, or {@code null} if unavailable
   * @throws ClassCastException if the instance cannot be cast to {@code iface}
   */
  @Nullable
  default <T> T unwrap(@NonNull Class<T> iface) {
    return iface.cast(unwrap());
  }

  /**
   * Unwraps the underlying field representation.
   *
   * <p>This method returns the actual instance represented by this field descriptor, or {@code
   * null} if no meaningful representation exists.
   *
   * @return the unwrapped instance if available, otherwise {@code null}.
   */
  @Nullable
  Object unwrap();

  /**
   * A singleton implementation of {@link FieldDescriptor} that represents an absent or unavailable
   * field. This is typically used as a placeholder when no actual field descriptor is applicable.
   *
   * <p>All retrieval methods return default values indicating the absence of meaningful field
   * metadata.
   */
  record AbsentFieldDescriptor() implements FieldDescriptor {

    @Override
    @NonNull
    public String getName() {
      return this.getClass().getSimpleName();
    }

    @Override
    @NonNull
    public Class<?> getType() {
      return this.getClass();
    }

    @Nullable
    @Override
    public Object unwrap() {
      return null;
    }

    @Override
    @Nullable
    public <T extends Annotation> T getAnnotation(@Nullable Class<T> annotationClass) {
      return null;
    }

    @Override
    @NonNull
    public Annotation[] getAnnotations() {
      return new Annotation[0];
    }

    @Override
    @NonNull
    public Annotation[] getDeclaredAnnotations() {
      return new Annotation[0];
    }
  }
}
