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
package tw.com.softleader.data.jpa.spec.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import tw.com.softleader.data.jpa.spec.SpecMapper;

/**
 * Interface to allow execution of Query by Spec instances.
 *
 * @author Matt Ho
 */
@NoRepositoryBean
public interface QueryBySpecExecutor<T> {

  /**
   * Returns a single entity matching the given {@code spec} or {@link Optional#empty()} if none found.
   *
   * @param spec the object will be mapped into {@link Specification} by {@link SpecMapper}
   * @return never {@literal null}.
   * @throws IncorrectResultSizeDataAccessException if more than one entity found.
   * @see JpaSpecificationExecutor#findOne(Specification)
   */
  Optional<T> findOneBySpec(@Nullable Object spec);

  /**
   * Returns all entities matching the given {@code spec}.
   *
   * @param spec the object will be mapped into {@link Specification} by {@link SpecMapper}
   * @return never {@literal null}.
   * @see JpaSpecificationExecutor#findAll(Specification)
   */
  List<T> findBySpec(@Nullable Object spec);

  /**
   * Returns a {@link Page} of entities matching the given {@code spec}.
   *
   * @param spec the object will be mapped into {@link Specification} by {@link SpecMapper}
   * @return never {@literal null}.
   * @see JpaSpecificationExecutor#findAll(Specification, Pageable)
   */
  Page<T> findBySpec(@Nullable Object spec, @NonNull Pageable pageable);

  /**
   * Returns all entities matching the given {@code spec} and {@link Sort}.
   *
   * @param spec the object will be mapped into {@link Specification} by {@link SpecMapper}
   * @return never {@literal null}.
   * @see JpaSpecificationExecutor#findAll(Specification, Sort)
   */
  List<T> findBySpec(@Nullable Object spec, @NonNull Sort sort);

  /**
   * Checks whether the data store contains elements that match the given {@code spec}.
   *
   * @param spec the object will be mapped into {@link Specification} by {@link SpecMapper}
   * @return the number of instances.
   * @see JpaSpecificationExecutor#count(Specification)
   */
  long countBySpec(@Nullable Object spec);

  /**
   * Checks whether the data store contains elements that match the given {@code spec}.
   *
   * @param spec the object will be mapped into {@link Specification} by {@link SpecMapper}
   * @return {@code true} if the data store contains elements that match the given {@link Specification} otherwise
   *         {@code false}.
   * @see JpaSpecificationExecutor#exists(Specification)
   */
  boolean existsBySpec(@Nullable Object spec);

  /**
   * Returns entities matching the given {@code spec} applying the {@code queryFunction} that defines the query
   * and its result type.
   *
   * @param spec the object will be mapped into {@link Specification} by {@link SpecMapper}
   * @param queryFunction the query function defining projection, sorting, and the result type
   * @return all entities matching the given Example.
   * @see JpaSpecificationExecutor#findBy(Specification, Function)
   */
  <S extends T, R> R findBySpec(@Nullable Object spec, @NonNull Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction);
}
