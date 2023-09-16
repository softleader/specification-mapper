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

import static java.util.concurrent.TimeUnit.MICROSECONDS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.SimpleSpecification;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.Gender;

@IntegrationTest
class CustomizeSimpleSpecTest {

  @Autowired
  CustomerRepository repository;

  SpecMapper mapper;
  SimpleSpecificationResolver simpleResolver;

  Customer matt;
  Customer bob;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder()
        .resolver(simpleResolver = spy(SimpleSpecificationResolver.class))
        .build();

    save(Customer.builder().name("matt").gender(Gender.MALE).createdTime(LocalDateTime.now()).build());
    save(Customer.builder().name("matt").gender(Gender.MALE).createdTime(LocalDateTime.now())
        .build());
    matt = save(Customer.builder().name("matt").gender(Gender.MALE).createdTime(LocalDateTime.now()).build());
    save(Customer.builder().name("bob").gender(Gender.MALE).createdTime(LocalDateTime.now()).build());
    bob = save(Customer.builder().name("bob").gender(Gender.MALE).createdTime(LocalDateTime.now())
        .build());
    save(Customer.builder().name("mary").gender(Gender.FEMALE).createdTime(LocalDateTime.now())
        .build());
  }

  @SneakyThrows
  private Customer save(@NonNull Customer customer) {
    try {
      return repository.save(customer);
    } finally {
      // 本 test 的邏輯會需要每次 CreateTime 都唯一, 避免發生 CreateTime 一樣造成測試失敗, 每次新增完固定等一下
      MICROSECONDS.sleep(1);
    }
  }

  @DisplayName("客製 Simple Spec")
  @Test
  void customizeSimpleSpec() {
    var criteria = MyCriteria.builder().gender(Gender.MALE).simpleMaxBy("name").build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, bob);

    verify(simpleResolver, times(2))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  public static class MaxCustomerCreatedTime extends SimpleSpecification<Customer> {

    public MaxCustomerCreatedTime(Context context, String path, Object value) {
      super(context, path, value);
    }

    @Override
    public Predicate toPredicate(Root<Customer> root,
        CriteriaQuery<?> query,
        CriteriaBuilder builder) {
      var subquery = query.subquery(LocalDateTime.class);
      var subroot = subquery.from(Customer.class);
      subquery.select(
          builder.greatest(subroot.get("createdTime").as(LocalDateTime.class)))
          .where(builder.equal(root.get((String) value), subroot.get((String) value)));
      return builder.equal(root.get("createdTime"), subquery);
    }
  }

  @Builder
  @Data
  public static class MyCriteria {

    @Spec
    Gender gender;

    @Spec(MaxCustomerCreatedTime.class)
    String simpleMaxBy;
  }
}
