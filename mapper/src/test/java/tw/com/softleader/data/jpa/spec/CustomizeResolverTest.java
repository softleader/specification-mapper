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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.Gender;

@IntegrationTest
class CustomizeResolverTest {

  @Autowired
  CustomerRepository repository;

  SpecMapper mapper;
  SimpleSpecificationResolver simpleResolver;
  MaxCreatedTimeSpecificationResolver maxCreatedTimeResolver;

  Customer matt;
  Customer bob;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder()
        .resolver(simpleResolver = spy(SimpleSpecificationResolver.class))
        .resolver(maxCreatedTimeResolver = spy(MaxCreatedTimeSpecificationResolver.class))
        .build();

    repository.save(
        Customer.builder().name("matt").gender(Gender.MALE).createdTime(LocalDateTime.now()).build());
    repository.save(
        Customer.builder().name("matt").gender(Gender.MALE).createdTime(LocalDateTime.now())
            .build());
    matt = repository.save(
        Customer.builder().name("matt").gender(Gender.MALE)
            .createdTime(LocalDateTime.now())
            .birthday(LocalDate.now())
            .build());
    repository.save(
        Customer.builder().name("bob").gender(Gender.MALE).createdTime(LocalDateTime.now()).build());
    bob = repository.save(
        Customer.builder().name("bob").gender(Gender.MALE).createdTime(LocalDateTime.now())
            .build());
    repository.save(
        Customer.builder().name("mary").gender(Gender.FEMALE).createdTime(LocalDateTime.now())
            .build());
  }

  @DisplayName("客製 Resolver")
  @Test
  void customizeResolver() {
    var criteria = MyCriteria.builder().gender(Gender.MALE).maxBy("name").build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, bob);

    var inOrder = inOrder(
        simpleResolver,
        maxCreatedTimeResolver);
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
    inOrder.verify(maxCreatedTimeResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.FIELD })
  public @interface MaxCreatedTime {

    Class<?> from();
  }

  @Builder
  @Data
  public static class MyCriteria {

    @Spec
    Gender gender;

    @MaxCreatedTime(from = Customer.class)
    String maxBy;
  }

  public static class MaxCreatedTimeSpecificationResolver implements SpecificationResolver {

    @Override
    public boolean supports(@NonNull Databind databind) {
      return databind.getField().isAnnotationPresent(MaxCreatedTime.class);
    }

    @Override
    public Specification<Object> buildSpecification(Context context, Databind databind) {
      var def = databind.getField().getAnnotation(MaxCreatedTime.class);
      return databind.getFieldValue()
          .map(value -> subquery(def.from(), value.toString()))
          .orElse(null);
    }

    Specification<Object> subquery(Class<?> entityClass, String by) {
      return (root, query, builder) -> {
        var subquery = query.subquery(Long.class);
        var subroot = subquery.from(entityClass);
        subquery.select(builder.max(subroot.get("createdTime")))
            .where(builder.equal(root.get(by), subroot.get(by)));
        return builder.equal(root.get("createdTime"), subquery);
      };
    }
  }
}
