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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.SpecMapper;
import tw.com.softleader.data.jpa.spec.starter.repository.support.DefaultQueryBySpecExecutor;
import tw.com.softleader.data.jpa.spec.starter.repository.support.QueryBySpecExecutorAdapter;

@Transactional
@EnableAutoConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(
    "spec.mapper.repository-base-class=tw.com.softleader.data.jpa.spec.starter.repository.CustomQueryBySpecExecutorAdapter")
class CustomQueryBySpecExecutorAdapterTest {

  @Autowired QueryBySpecExecutor<?> executor;

  @Test
  void shouldGetCustomQueryBySpecExecutorAdapter() {
    var actual = getUltimateTargetObject(executor);
    assertThat(actual)
        .isNotInstanceOf(DefaultQueryBySpecExecutor.class)
        .isInstanceOf(CustomQueryBySpecExecutorAdapter.class);
  }
}

class CustomQueryBySpecExecutorAdapter extends SimpleJpaRepository<Object, Long>
    implements QueryBySpecExecutorAdapter<Object> {

  public CustomQueryBySpecExecutorAdapter(
      JpaEntityInformation<Object, ?> entityInformation, EntityManager entityManager) {
    super(entityInformation, entityManager);
  }

  @Override
  public SpecMapper getSpecMapper() {
    return null;
  }

  @Override
  public void setSpecMapper(SpecMapper specMapper) {}

  @Override
  public Class<Object> getDomainClass() {
    return null;
  }
}
