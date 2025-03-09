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

import jakarta.persistence.criteria.JoinType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a fetch operation on an entity type or fields, allowing eager loading of related
 * entities through SQL joins. This annotation is useful for optimizing queries by preloading
 * associated data instead of triggering multiple lazy-loading queries.
 *
 * <p>By default, {@code @JoinFetch} uses an {@code INNER JOIN} and ensures distinct results. The
 * join type and distinct behavior can be customized using {@link #joinType()} and {@link
 * #distinct()}.
 *
 * <h3>Usage Example</h3>
 *
 * <pre>{@code
 * @JoinFetch(path = "orders", alias = "o")
 * public class CustomerOrderCriteria {
 *
 *   @Spec(path = "o.itemName", value = In.class)
 *   Collection<String> items;
 * }
 * }</pre>
 *
 * @author Matt Ho
 * @see JoinFetches
 * @see Join
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface JoinFetch {

  /**
   * Specifies a collection property to join on, e.g., "addresses". This should match an entity
   * association in the domain model.
   */
  String path();

  /**
   * Specifies an alias for the joined entity, e.g., "a". If not specified, the {@link #path()} will
   * be used with dots replaced by underscores.
   */
  String alias() default "";

  /**
   * Whether the query should return distinct results. Defaults to {@code true} to prevent duplicate
   * records.
   */
  boolean distinct() default true;

  /** Specifies the type of join to use in the join operation. Defaults to {@code INNER JOIN}. */
  JoinType joinType() default JoinType.INNER;

  /**
   * Container annotation for defining multiple {@link JoinFetch} annotations on the same element.
   *
   * <p>This is useful when multiple relationships need to be eagerly fetched within the same
   * entity.
   *
   * @author Matt Ho
   * @see JoinFetch
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.TYPE})
  @interface JoinFetches {

    /** An array of {@link JoinFetch} annotations to apply multiple join fetches. */
    JoinFetch[] value();
  }
}
