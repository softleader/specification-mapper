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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.FluentQuery;

/**
 * This class is meant to ensure that all methods in {@link JpaSpecificationExecutor} are either used or considered in
 * {@link QueryBySpecExecutor}.
 *
 * <p>
 * <br>
 * If methods in {@link JpaSpecificationExecutor} change in the future, this class will trigger a compilation error,
 * alerting the maintainers that some adjustments are required.
 *
 * @author Matt Ho
 */
record EnsureAllMethodsAreConsidered() implements JpaSpecificationExecutor<Object> {
  @Override
  public Optional<Object> findOne(Specification<Object> spec) {
    return Optional.empty();
  }

  @Override
  public List<Object> findAll(Specification<Object> spec) {
    return null;
  }

  @Override
  public Page<Object> findAll(Specification<Object> spec, Pageable pageable) {
    return null;
  }

  @Override
  public List<Object> findAll(Specification<Object> spec, Sort sort) {
    return null;
  }

  @Override
  public long count(Specification<Object> spec) {
    return 0;
  }

  @Override
  public boolean exists(Specification<Object> spec) {
    return false;
  }

  @Override
  public long delete(Specification<Object> spec) {
    return 0;
  }

  @Override
  public <S, R> R findBy(Specification<Object> spec, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
    return null;
  }
}
