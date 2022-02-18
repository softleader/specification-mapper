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
import static org.mockito.Mockito.verify;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDateTime;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.SimpleSpecification;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.Gender;

@Transactional
@IntegrationTest
class CustomizationTest {

  @Autowired
  CustomerRepository repository;

  SpecMapper mapper;
  SimpleSpecificationResolver simpleResolver;
  MaxCreatedTimeSpecificationResolver customizeResolver;

  Customer matt;
  Customer bob;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder()
        .resolver(simpleResolver = spy(SimpleSpecificationResolver.class))
        .resolver(customizeResolver = spy(MaxCreatedTimeSpecificationResolver.class))
        .build();

    repository.save(
        Customer.builder().name("matt").gender(Gender.MALE).createdTime(LocalDateTime.now()).build());
    repository.save(
        Customer.builder().name("matt").gender(Gender.MALE).createdTime(LocalDateTime.now()).build());
    matt = repository.save(
        Customer.builder().name("matt").gender(Gender.MALE).createdTime(LocalDateTime.now()).build());
    repository.save(
        Customer.builder().name("bob").gender(Gender.MALE).createdTime(LocalDateTime.now()).build());
    bob = repository.save(
        Customer.builder().name("bob").gender(Gender.MALE).createdTime(LocalDateTime.now()).build());
    repository.save(
        Customer.builder().name("mary").gender(Gender.FEMALE).createdTime(LocalDateTime.now())
            .build());
  }

  @DisplayName("客製 Simple Spec")
  @Test
  void customizeSimpleSpec() {
    var criteria = MyCriteria.builder().gender(Gender.MALE).simpleMaxBy("name").build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, bob);

    var inOrder = inOrder(
        simpleResolver,
        customizeResolver);
    inOrder.verify(simpleResolver, times(2))
        .buildSpecification(any(Context.class), any(Databind.class));
    verify(customizeResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("客製 Resolver")
  @Test
  void customizeSpecificationResolver() {
    var criteria = MyCriteria.builder().gender(Gender.MALE).maxBy("name").build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, bob);

    var inOrder = inOrder(
        simpleResolver,
        customizeResolver);
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
    inOrder.verify(customizeResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.FIELD })
  public @interface MaxCreatedTime {

    Class<?> from();
  }

  public static class MaxCustomerCreatedTime extends SimpleSpecification<Customer> {

    public MaxCustomerCreatedTime(Context context, String path, Object value) {
      super(context, path, value);
    }

    @Override
    public Predicate toPredicate(Root<Customer> root,
        CriteriaQuery<?> query,
        CriteriaBuilder builder) {
      var subquery = query.subquery(Long.class);
      var subroot = subquery.from(Customer.class);
      subquery.select(builder.max(subroot.get("createdTime")))
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
