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
package tw.com.softleader.data.jpa.spec.repository.support;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.SpecMapper;
import tw.com.softleader.data.jpa.spec.repository.QueryBySpecExecutor;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.springframework.util.Assert.notNull;

/**
 * Default implementation of {@code QueryBySpecExecutor}
 *
 * @author Matt Ho
 */
public interface QueryBySpecExecutorAdapter<T> extends JpaSpecificationExecutor<T>,
    QueryBySpecExecutor<T> {

  @Override
  @Transactional(readOnly = true)
  default List<T> findBySpec(@Nullable Object spec) {
    var mapper = getSpecMapper();
    var domainClass = getDomainClass();
    notNull(mapper, "getSpecMapper() must not returns null");
    notNull(domainClass, "getDomainClass() must not returns null");
    return findAll(mapper.toSpec(spec, domainClass));
  }

  @Override
  @Transactional(readOnly = true)
  default List<T> findBySpec(@Nullable Object spec, @NonNull Sort sort) {
    var mapper = getSpecMapper();
    var domainClass = getDomainClass();
    notNull(mapper, "getSpecMapper() must not returns null");
    notNull(domainClass, "getDomainClass() must not returns null");
    return findAll(mapper.toSpec(spec, domainClass), sort);
  }

  @Override
  @Transactional(readOnly = true)
  default Page<T> findBySpec(@Nullable Object spec, @NonNull Pageable pageable) {
    var mapper = getSpecMapper();
    var domainClass = getDomainClass();
    notNull(mapper, "getSpecMapper() must not returns null");
    notNull(domainClass, "getDomainClass() must not returns null");
    return findAll(mapper.toSpec(spec, domainClass), pageable);
  }

  @Override
  @Transactional(readOnly = true)
  default long countBySpec(@Nullable Object spec) {
    var mapper = getSpecMapper();
    var domainClass = getDomainClass();
    notNull(mapper, "getSpecMapper() must not returns null");
    notNull(domainClass, "getDomainClass() must not returns null");
    return count(mapper.toSpec(spec, domainClass));
  }

  @Override
  @Transactional(readOnly = true)
  default boolean existsBySpec(@Nullable Object spec) {
    var mapper = getSpecMapper();
    var domainClass = getDomainClass();
    notNull(mapper, "getSpecMapper() must not returns null");
    notNull(domainClass, "getDomainClass() must not returns null");
    return exists(mapper.toSpec(spec, domainClass));
  }

  @Override
  @Transactional(readOnly = true)
  default Optional<T> findOneBySpec(@Nullable Object spec) {
    var mapper = getSpecMapper();
    var domainClass = getDomainClass();
    notNull(mapper, "getSpecMapper() must not returns null");
    notNull(domainClass, "getDomainClass() must not returns null");
    return findOne(mapper.toSpec(spec, domainClass));
  }

  @Override
  @Transactional(readOnly = true)
  default <S extends T, R> R findBySpec(@Nullable Object spec,
      @NonNull Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
    var mapper = getSpecMapper();
    var domainClass = getDomainClass();
    notNull(mapper, "getSpecMapper() must not returns null");
    notNull(domainClass, "getDomainClass() must not returns null");
    return findBy(mapper.toSpec(spec, domainClass), queryFunction);
  }

  SpecMapper getSpecMapper();

  void setSpecMapper(@org.springframework.lang.NonNull SpecMapper specMapper);

  Class<T> getDomainClass();
}
