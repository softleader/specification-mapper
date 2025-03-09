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
package tw.com.softleader.data.jpa.spec.annotation;

import jakarta.persistence.criteria.Path;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.domain.Equals;
import tw.com.softleader.data.jpa.spec.domain.SimpleSpecification;

/**
 * Annotation for specifying query filter on entity fields.
 *
 * @author Matt Ho
 * @see NestedSpec
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Spec {

  /**
   * The attribute name used to construct a {@code Path} for querying.
   *
   * <p>If left empty, the field name will be used by default.
   *
   * @return the attribute name for the query path
   * @see Path
   */
  String path() default "";

  /**
   * The {@link SimpleSpecification} implementation to be applied.
   *
   * @return the specification class
   */
  Class<? extends SimpleSpecification> value() default Equals.class;

  /**
   * Whether to negate the specified {@link Specification}.
   *
   * <p>If set to {@code true}, the generated query condition will be negated.
   *
   * @return {@code true} to negate the specification, otherwise {@code false}
   * @see Specification#not(Specification)
   */
  boolean not() default false;
}
