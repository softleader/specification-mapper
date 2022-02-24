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

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.Data;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.SpecMapper;
import tw.com.softleader.data.jpa.spec.SpecificationResolver;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Disjunction;

@Transactional
@EnableAutoConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest
class CustomizationTest {

  @Autowired
  SpecMapper mapper;

  @Test
  void customizeResolver() {
    var spec = mapper.toSpec(new MyCriteria());
    assertThat(spec)
        .isNotNull()
        .isInstanceOfAny(Conjunction.class, Disjunction.class)
        .hasFieldOrProperty("specs")
        .extracting("specs")
        .asInstanceOf(InstanceOfAssertFactories.COLLECTION)
        .hasSize(1)
        .first()
        .isInstanceOf(MySpecification.class);
  }

  @Data
  static class MyCriteria {

    String hello;
  }

  @Configuration
  static class MyConfig {

    @Bean
    SpecificationResolver myResolver() {
      return SpecificationResolver.builder()
          .supports(databind -> true)
          .buildSpecification((context, databind) -> new MySpecification())
          .build();
    }
  }

  static class MySpecification implements Specification<Object> {

    @Override
    public Predicate toPredicate(
        Root<Object> root, CriteriaQuery<?> query,
        CriteriaBuilder builder) {
      return null;
    }
  }

}
