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
package tw.com.softleader.data.jpa.spec.starter.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.AopTestUtils.getUltimateTargetObject;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.starter.repository.support.DefaultQueryBySpecExecutor;

@Transactional
@EnableAutoConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(
    "spec.mapper.repository-base-class=tw.com.softleader.data.jpa.spec.starter.repository.CustomQueryBySpecExecutor")
class CustomQueryBySpecExecutorTest {

  @Autowired QueryBySpecExecutor<?> executor;

  @Test
  void shouldGetCustomQueryBySpecExecutorAdapter() {
    var actual = getUltimateTargetObject(executor);
    assertThat(actual)
        .isNotInstanceOf(DefaultQueryBySpecExecutor.class)
        .isInstanceOf(CustomQueryBySpecExecutor.class);
  }
}

class CustomQueryBySpecExecutor extends SimpleJpaRepository<Object, Long>
    implements QueryBySpecExecutor<Object> {

  public CustomQueryBySpecExecutor(
      JpaEntityInformation<Object, ?> entityInformation, EntityManager entityManager) {
    super(entityInformation, entityManager);
  }

  @Override
  public Optional<Object> findOneBySpec(Object spec) {
    return Optional.empty();
  }

  @Override
  public List<Object> findBySpec(Object spec) {
    return List.of();
  }

  @Override
  public Page<Object> findBySpec(Object spec, Pageable pageable) {
    return null;
  }

  @Override
  public Page<Object> findBySpec(Object spec, Object countSpec, Pageable pageable) {
    return null;
  }

  @Override
  public List<Object> findBySpec(Object spec, Sort sort) {
    return List.of();
  }

  @Override
  public long countBySpec(Object spec) {
    return 0;
  }

  @Override
  public boolean existsBySpec(Object spec) {
    return false;
  }

  @Override
  public <S, R> R findBySpec(
      Object spec, Function<? super SpecificationFluentQuery<S>, R> queryFunction) {
    return null;
  }
}
