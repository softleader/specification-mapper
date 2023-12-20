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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.annotation.NestedSpec;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.Gender;

@IntegrationTest
class CustomizeResolverTest {

  @Autowired CustomerRepository repository;

  SpecMapper mapper;
  SimpleSpecificationResolver simpleResolver;
  MaxCreatedTimeSpecificationResolver maxCreatedTimeResolver;
  NestedSpecificationResolver nestedResolver;

  Customer matt;
  Customer bob;
  Customer mary;

  @BeforeEach
  void setup() {
    mapper =
        SpecMapper.builder()
            .resolver(simpleResolver = spy(SimpleSpecificationResolver.class))
            .resolver(maxCreatedTimeResolver = spy(MaxCreatedTimeSpecificationResolver.class))
            .build();

    save(
        Customer.builder()
            .name("matt")
            .gender(Gender.MALE)
            .createdTime(LocalDateTime.now())
            .build());
    save(
        Customer.builder()
            .name("matt")
            .gender(Gender.MALE)
            .createdTime(LocalDateTime.now())
            .build());
    matt =
        save(
            Customer.builder()
                .name("matt")
                .gender(Gender.MALE)
                .createdTime(LocalDateTime.now())
                .birthday(LocalDate.now())
                .build());
    save(
        Customer.builder()
            .name("bob")
            .gender(Gender.MALE)
            .createdTime(LocalDateTime.now())
            .build());
    bob =
        save(
            Customer.builder()
                .name("bob")
                .gender(Gender.MALE)
                .createdTime(LocalDateTime.now())
                .build());
    mary =
        save(
            Customer.builder()
                .name("mary")
                .gender(Gender.FEMALE)
                .createdTime(LocalDateTime.now())
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

  @DisplayName("客製 Resolver")
  @Test
  void customizeResolver() {
    var criteria = MyCriteria.builder().gender(Gender.MALE).maxBy("name").build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, bob);

    var inOrder = inOrder(simpleResolver, maxCreatedTimeResolver);
    inOrder
        .verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
    inOrder
        .verify(maxCreatedTimeResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("客製 Resolver 在 Type 同時用了 NestedSpec")
  @Test
  void customizeResolverOnTypeAndNested() {
    CustomizeOnTypeSpecificationResolver customizeOnTypeResolver;
    mapper =
        SpecMapper.builder()
            .resolver(simpleResolver = spy(SimpleSpecificationResolver.class))
            .resolver(codec -> nestedResolver = spy(new NestedSpecificationResolver(codec)))
            .resolver(customizeOnTypeResolver = spy(CustomizeOnTypeSpecificationResolver.class))
            .build();

    var criteria =
        OuterCriteria.builder()
            .gender(Gender.FEMALE)
            .inner(InnerCriteria.builder().gender(Gender.FEMALE).build())
            .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();

    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(mary);

    verify(nestedResolver, times(1)).buildSpecification(any(Context.class), any(Databind.class));

    // 跟掛 annotation 的 class fields 數一樣
    verify(customizeOnTypeResolver, times(2))
        .buildSpecification(any(Context.class), any(Databind.class));

    verify(customizeOnTypeResolver, times(1)).buildSpecification();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD})
  public @interface MaxCreatedTime {

    Class<?> from();
  }

  @Builder
  @Data
  public static class MyCriteria {

    @Spec Gender gender;

    @MaxCreatedTime(from = Customer.class)
    String maxBy;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE})
  public @interface CustomizeOnType {}

  public static class MaxCreatedTimeSpecificationResolver implements SpecificationResolver {

    @Override
    public boolean supports(@NonNull Databind databind) {
      return databind.getField().isAnnotationPresent(MaxCreatedTime.class);
    }

    @Override
    public Specification<Object> buildSpecification(Context context, Databind databind) {
      var def = databind.getField().getAnnotation(MaxCreatedTime.class);
      return databind
          .getFieldValue()
          .map(value -> subquery(def.from(), value.toString()))
          .orElse(null);
    }

    Specification<Object> subquery(Class<?> entityClass, String by) {
      return (root, query, builder) -> {
        var subquery = query.subquery(LocalDateTime.class);
        var subroot = subquery.from(entityClass);
        subquery
            .select(builder.greatest(subroot.get("createdTime").as(LocalDateTime.class)))
            .where(builder.equal(root.get(by), subroot.get(by)));
        return builder.equal(root.get("createdTime"), subquery);
      };
    }
  }

  @Builder
  @CustomizeOnType
  static class OuterCriteria {

    @Spec Gender gender;

    @NestedSpec InnerCriteria inner;
  }

  @Builder
  static class InnerCriteria {

    @Spec Gender gender;
  }

  public static class CustomizeOnTypeSpecificationResolver implements SpecificationResolver {

    static final String BUILT = CustomizeOnTypeSpecificationResolver.class.getTypeName();

    @Override
    public boolean supports(@NonNull Databind databind) {
      return databind.getTarget().getClass().isAnnotationPresent(CustomizeOnType.class);
    }

    @Override
    public Specification<Object> buildSpecification(Context context, Databind databind) {
      if (context.containsKey(BUILT)) {
        return null;
      }
      try {
        return buildSpecification();
      } finally {
        context.put(BUILT, new Object());
      }
    }

    Specification<Object> buildSpecification() {
      return (root, query, builder) -> builder.isNotNull(root.get("gender"));
    }
  }
}
