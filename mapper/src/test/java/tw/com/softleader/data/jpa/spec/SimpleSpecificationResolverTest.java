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
import static org.mockito.Mockito.*;
import static org.springframework.util.ReflectionUtils.doWithLocalFields;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.Builder;
import lombok.Data;
import tw.com.softleader.data.jpa.spec.annotation.And;
import tw.com.softleader.data.jpa.spec.annotation.Or;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.After;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.In;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.Gender;

@IntegrationTest
class SimpleSpecificationResolverTest {

  @Autowired
  CustomerRepository repository;

  SpecMapper mapper;
  SimpleSpecificationResolver simpleResolver;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder()
        .resolver(simpleResolver = spy(SimpleSpecificationResolver.class))
        .build();
  }

  @DisplayName("Null Root Object")
  @Test
  void nullRootObject() {
    assertThat(mapper.toSpec(null))
        .isNull();
  }

  @DisplayName("空的 @Spec")
  @Test
  void emptySpec() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = MyCriteria.builder().name(matt.getName()).build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);

    verify(simpleResolver, times(numberOfLocalField(MyCriteria.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("空的 Criteria")
  @Test
  void emptyCriteria() {
    var criteria = MyCriteria.builder().build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNull();
  }

  @DisplayName("Optional Empty")
  @Test
  void optionalEmpty() {
    var criteria = MyCriteria.builder()
        .opt(Optional.empty())
        .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNull();
  }

  @DisplayName("Optional Present")
  @Test
  void optionalPresent() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = MyCriteria.builder()
        .opt(Optional.of("matt"))
        .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);

    verify(simpleResolver, times(numberOfLocalField(MyCriteria.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("Iterable Empty")
  @Test
  void iterableEmpty() {
    var criteria = MyCriteria.builder()
        .names(Arrays.asList())
        .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNull();
  }

  @DisplayName("Iterable Present")
  @Test
  void iterablePresent() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = MyCriteria.builder()
        .names(Arrays.asList("matt"))
        .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);

    verify(simpleResolver, times(numberOfLocalField(MyCriteria.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("Negation of spec")
  @Test
  void notSpec() {
    var matt = repository.save(Customer.builder().name("matt").birthday(LocalDate.now()).build());
    repository.save(Customer.builder().name("bob").birthday(LocalDate.now().plusDays(1)).build());
    var mary = repository.save(
        Customer.builder().name("mary").birthday(LocalDate.now().minusDays(1)).build());

    var criteria = MyCriteria.builder()
        .birthday(LocalDate.now())
        .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, mary);

    verify(simpleResolver, times(numberOfLocalField(MyCriteria.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("Force Or")
  @Test
  void forceOr() {
    var matt = repository.save(
        Customer.builder().name("matt").gender(Gender.MALE).birthday(LocalDate.now()).build());
    var bob = repository.save(
        Customer.builder().name("bob").gender(Gender.MALE).birthday(LocalDate.now().plusDays(1))
            .build());
    var mary = repository.save(
        Customer.builder().name("mary").gender(Gender.FEMALE).birthday(LocalDate.now().minusDays(1))
            .build());

    var criteria = ForceOr.builder()
        .name(bob.getName())
        .gender(bob.getGender())
        .birthday(LocalDate.now())
        .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(3).contains(matt, bob, mary);

    verify(simpleResolver, times(numberOfLocalField(ForceOr.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("Force Or 2")
  @Test
  void forceOr2() {
    var matt = repository.save(
        Customer.builder().name("matt").gender(Gender.MALE).birthday(LocalDate.now()).build());
    var bob = repository.save(
        Customer.builder().name("bob").gender(Gender.MALE).birthday(LocalDate.now().plusDays(1))
            .build());
    repository.save(
        Customer.builder().name("mary").gender(Gender.FEMALE).birthday(LocalDate.now().minusDays(1))
            .build());

    var criteria = ForceOr2.builder()
        .name(bob.getName())
        .gender(bob.getGender())
        .birthday(LocalDate.now())
        .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, bob);

    verify(simpleResolver, times(numberOfLocalField(ForceOr2.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("Force And")
  @Test
  void forceAnd() {
    var matt = repository.save(
        Customer.builder().name("matt").gender(Gender.MALE).birthday(LocalDate.now()).build());
    repository.save(
        Customer.builder().name("bob").gender(Gender.MALE).birthday(LocalDate.now().plusDays(1))
            .build());
    var mary = repository.save(
        Customer.builder().name("mary").gender(Gender.FEMALE).birthday(LocalDate.now().minusDays(1))
            .build());

    var criteria = ForceAnd.builder()
        .name(matt.getName())
        .gender(mary.getGender())
        .birthday(LocalDate.now())
        .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, mary);

    verify(simpleResolver, times(numberOfLocalField(ForceAnd.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  int numberOfLocalField(@Nonnull Class<?> clazz) {
    var i = new AtomicInteger();
    doWithLocalFields(clazz, f -> i.getAndIncrement());
    return i.intValue();
  }

  @DisplayName("Skip if empty text")
  @Test
  void skipIfEmptyText() {
    var matt = repository.save(
        Customer.builder().name("matt").gender(Gender.MALE).birthday(LocalDate.now()).build());
    var bob = repository.save(
        Customer.builder().name("bob").gender(Gender.MALE).birthday(LocalDate.now().plusDays(1))
            .build());
    var mary = repository.save(
        Customer.builder().name("mary").gender(Gender.FEMALE).birthday(LocalDate.now().minusDays(1))
            .build());

    var criteria = SkipEmptyText.builder().name("").build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(3).contains(matt, bob, mary);

    verify(simpleResolver, times(numberOfLocalField(SkipEmptyText.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("Not to skip if blank text")
  @Test
  void notToSkipIfBlackText() {
    repository.save(
        Customer.builder().name("matt").gender(Gender.MALE).birthday(LocalDate.now()).build());
    repository.save(
        Customer.builder().name("bob").gender(Gender.MALE).birthday(LocalDate.now().plusDays(1))
            .build());
    repository.save(
        Customer.builder().name("mary").gender(Gender.FEMALE).birthday(LocalDate.now().minusDays(1))
            .build());

    var criteria = SkipEmptyText.builder().name(" ").build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).isEmpty();

    verify(simpleResolver, times(numberOfLocalField(SkipEmptyText.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @Builder
  @Data
  public static class MyCriteria {

    @Spec
    String name;

    @Spec(path = "name")
    Optional<String> opt;

    @Spec(path = "name", value = In.class)
    Collection<String> names;

    @Spec(value = After.class, not = true)
    LocalDate birthday;
  }

  @Builder
  @Data
  public static class ForceOr {

    @Spec
    String name;

    @Spec
    Gender gender;

    @Or
    @Spec(value = After.class, not = true)
    LocalDate birthday;
  }

  @Builder
  @Data
  public static class ForceOr2 {

    @Spec
    String name;

    @Or
    @Spec(value = After.class, not = true)
    LocalDate birthday;

    @Spec
    Gender gender;
  }

  @Or
  @Builder
  @Data
  public static class ForceAnd {

    @Spec
    String name;

    @Spec
    Gender gender;

    @And
    @Spec(value = After.class, not = true)
    LocalDate birthday;
  }

  @Builder
  @Data
  public static class SkipEmptyText {

    @Spec
    String name;
  }
}
