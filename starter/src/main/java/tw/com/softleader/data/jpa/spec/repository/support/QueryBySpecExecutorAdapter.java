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

import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.SpecMapper;
import tw.com.softleader.data.jpa.spec.repository.QueryBySpecExecutor;

/**
 * Default implementation of {@code QueryBySpecExecutor}
 *
 * @author Matt Ho
 */
public interface QueryBySpecExecutorAdapter<T> extends JpaSpecificationExecutor<T>,
    QueryBySpecExecutor<T> {

  @Override
  @Transactional(readOnly = true)
  default List<T> findBySpec(Object spec) {
    return findAll(getSpecMapper().toSpec(spec, getDomainClass()));
  }

  @Override
  @Transactional(readOnly = true)
  default List<T> findBySpec(Object spec, @NonNull Sort sort) {
    return findAll(getSpecMapper().toSpec(spec, getDomainClass()), sort);
  }

  @Override
  @Transactional(readOnly = true)
  default Page<T> findBySpec(Object spec, @NonNull Pageable pageable) {
    return findAll(getSpecMapper().toSpec(spec, getDomainClass()), pageable);
  }

  @Override
  @Transactional(readOnly = true)
  default long countBySpec(Object spec) {
    return count(getSpecMapper().toSpec(spec, getDomainClass()));
  }

  @Override
  @Transactional(readOnly = true)
  default boolean existsBySpec(Object spec) {
    return count(getSpecMapper().toSpec(spec, getDomainClass())) > 0;
  }

  SpecMapper getSpecMapper();

  void setSpecMapper(SpecMapper specMapper);

  Class<T> getDomainClass();
}
