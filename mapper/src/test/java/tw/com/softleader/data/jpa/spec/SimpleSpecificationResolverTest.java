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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.util.ReflectionUtils.doWithLocalFields;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tw.com.softleader.data.jpa.spec.annotation.And;
import tw.com.softleader.data.jpa.spec.annotation.Or;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.*;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.Gender;

@IntegrationTest
class SimpleSpecificationResolverTest {

  @Autowired CustomerRepository repository;

  SpecMapper mapper;
  SimpleSpecificationResolver simpleResolver;

  @BeforeEach
  void setup() {
    mapper =
        SpecMapper.builder()
            .resolver(simpleResolver = spy(SimpleSpecificationResolver.class))
            .build();
  }

  @DisplayName("Null Root Object")
  @Test
  void nullRootObject() {
    assertThat(mapper.toSpec(null)).isNull();
  }

  @DisplayName("空的 @Spec")
  @Test
  void emptySpec() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = MyCriteria.builder().name(matt.getName()).build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec)
        .isNotNull()
        .isInstanceOf(Conjunction.class)
        .extracting("specs", LIST)
        .hasSize(1)
        .first()
        .isInstanceOf(Equals.class);
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
    var criteria = MyCriteria.builder().opt(Optional.empty()).build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNull();
  }

  @DisplayName("Optional Present")
  @Test
  void optionalPresent() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = MyCriteria.builder().opt(Optional.of("matt")).build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec)
        .isNotNull()
        .isInstanceOf(Conjunction.class)
        .extracting("specs", LIST)
        .hasSize(1)
        .first()
        .isInstanceOf(Equals.class);
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);

    verify(simpleResolver, times(numberOfLocalField(MyCriteria.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("Iterable Empty")
  @Test
  void iterableEmpty() {
    var criteria = MyCriteria.builder().names(Arrays.asList()).build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNull();
  }

  @DisplayName("Iterable Present")
  @Test
  void iterablePresent() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = MyCriteria.builder().names(Arrays.asList("matt")).build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec)
        .isNotNull()
        .isInstanceOf(Conjunction.class)
        .extracting("specs", LIST)
        .hasSize(1)
        .first()
        .isInstanceOf(In.class);
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
    var mary =
        repository.save(
            Customer.builder().name("mary").birthday(LocalDate.now().minusDays(1)).build());

    var criteria = MyCriteria.builder().birthday(LocalDate.now()).build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec)
        .isNotNull()
        .isInstanceOf(Conjunction.class)
        .extracting("specs", LIST)
        .hasSize(1)
        .first()
        .isInstanceOf(Not.class)
        .extracting("spec")
        .isInstanceOf(After.class);
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, mary);

    verify(simpleResolver, times(numberOfLocalField(MyCriteria.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("Force Or")
  @Test
  void forceOr() {
    var matt =
        repository.save(
            Customer.builder().name("matt").gender(Gender.MALE).birthday(LocalDate.now()).build());
    var bob =
        repository.save(
            Customer.builder()
                .name("bob")
                .gender(Gender.MALE)
                .birthday(LocalDate.now().plusDays(1))
                .build());
    var mary =
        repository.save(
            Customer.builder()
                .name("mary")
                .gender(Gender.FEMALE)
                .birthday(LocalDate.now().minusDays(1))
                .build());

    var criteria =
        ForceOr.builder()
            .name(bob.getName())
            .gender(bob.getGender())
            .birthday(LocalDate.now())
            .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    var depth1 =
        assertThat(spec)
            .isNotNull()
            .isInstanceOf(Conjunction.class)
            .extracting("specs", LIST)
            .hasSize(3);
    depth1.first().isInstanceOf(Equals.class);
    depth1.element(1).isInstanceOf(Equals.class);
    depth1
        .element(2)
        .isInstanceOf(tw.com.softleader.data.jpa.spec.domain.Or.class)
        .extracting("spec")
        .isInstanceOf(Not.class)
        .extracting("spec")
        .isInstanceOf(After.class);
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(3).contains(matt, bob, mary);

    verify(simpleResolver, times(numberOfLocalField(ForceOr.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("Force Or 2")
  @Test
  void forceOr2() {
    var matt =
        repository.save(
            Customer.builder().name("matt").gender(Gender.MALE).birthday(LocalDate.now()).build());
    var bob =
        repository.save(
            Customer.builder()
                .name("bob")
                .gender(Gender.MALE)
                .birthday(LocalDate.now().plusDays(1))
                .build());
    var mary =
        repository.save(
            Customer.builder()
                .name("mary")
                .gender(Gender.FEMALE)
                .birthday(LocalDate.now().minusDays(1))
                .build());

    var criteria =
        ForceOr2.builder()
            .name(bob.getName())
            .gender(bob.getGender())
            .birthday(LocalDate.now())
            .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    var depth1 =
        assertThat(spec)
            .isNotNull()
            .isInstanceOf(Conjunction.class)
            .extracting("specs", LIST)
            .hasSize(3);
    depth1.first().isInstanceOf(Equals.class);
    depth1
        .element(1)
        .isInstanceOf(tw.com.softleader.data.jpa.spec.domain.Or.class)
        .extracting("spec")
        .isInstanceOf(Not.class)
        .extracting("spec")
        .isInstanceOf(After.class);
    depth1.element(2).isInstanceOf(Equals.class);
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(3).contains(matt, bob, mary);

    verify(simpleResolver, times(numberOfLocalField(ForceOr2.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("Force Or 3 - @Or 宣告在第一順位")
  @Test
  void forceOr3() {
    var matt =
        repository.save(
            Customer.builder().name("matt").gender(Gender.MALE).birthday(LocalDate.now()).build());
    var bob =
        repository.save(
            Customer.builder()
                .name("bob")
                .gender(Gender.MALE)
                .birthday(LocalDate.now().plusDays(1))
                .build());
    var mary =
        repository.save(
            Customer.builder()
                .name("mary")
                .gender(Gender.FEMALE)
                .birthday(LocalDate.now().minusDays(1))
                .build());

    var criteria =
        ForceOr3.builder()
            .name(bob.getName())
            .gender(bob.getGender())
            .birthday(LocalDate.now())
            .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    var depth1 =
        assertThat(spec)
            .isNotNull()
            .isInstanceOf(Conjunction.class)
            .extracting("specs", LIST)
            .hasSize(3);
    depth1
        .first()
        .isInstanceOf(tw.com.softleader.data.jpa.spec.domain.Or.class)
        .extracting("spec")
        .isInstanceOf(Not.class)
        .extracting("spec")
        .isInstanceOf(After.class);
    depth1.element(1).isInstanceOf(Equals.class);
    depth1.element(2).isInstanceOf(Equals.class);
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(3).contains(matt, bob, mary);

    verify(simpleResolver, times(numberOfLocalField(ForceOr3.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("@Or 無論宣告在哪個順位都得到相同的結果")
  @Test
  void forceOrIsPermutationInvariant() {
    repository.save(
        Customer.builder().name("matt").gender(Gender.MALE).birthday(LocalDate.now()).build());
    repository.save(
        Customer.builder()
            .name("bob")
            .gender(Gender.MALE)
            .birthday(LocalDate.now().plusDays(1))
            .build());
    repository.save(
        Customer.builder()
            .name("mary")
            .gender(Gender.FEMALE)
            .birthday(LocalDate.now().minusDays(1))
            .build());

    var birthday = LocalDate.now();
    var expected =
        repository.findAll(
            mapper.toSpec(
                ForceOr.builder().name("bob").gender(Gender.MALE).birthday(birthday).build(),
                Customer.class));
    assertThat(expected).isNotEmpty();
    assertThat(
            repository.findAll(
                mapper.toSpec(
                    ForceOr2.builder().name("bob").gender(Gender.MALE).birthday(birthday).build(),
                    Customer.class)))
        .containsExactlyInAnyOrderElementsOf(expected);
    assertThat(
            repository.findAll(
                mapper.toSpec(
                    ForceOr3.builder().name("bob").gender(Gender.MALE).birthday(birthday).build(),
                    Customer.class)))
        .containsExactlyInAnyOrderElementsOf(expected);
  }

  @DisplayName("Force And")
  @Test
  void forceAnd() {
    var matt =
        repository.save(
            Customer.builder().name("matt").gender(Gender.MALE).birthday(LocalDate.now()).build());
    repository.save(
        Customer.builder()
            .name("bob")
            .gender(Gender.MALE)
            .birthday(LocalDate.now().plusDays(1))
            .build());
    var mary =
        repository.save(
            Customer.builder()
                .name("mary")
                .gender(Gender.FEMALE)
                .birthday(LocalDate.now().minusDays(1))
                .build());

    var criteria =
        ForceAnd.builder()
            .name(matt.getName())
            .gender(mary.getGender())
            .birthday(LocalDate.now())
            .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    var depth1 =
        assertThat(spec)
            .isNotNull()
            .isInstanceOf(Disjunction.class)
            .extracting("specs", LIST)
            .hasSize(3);
    depth1.first().isInstanceOf(Equals.class);
    depth1.element(1).isInstanceOf(Equals.class);
    depth1
        .element(2)
        .isInstanceOf(tw.com.softleader.data.jpa.spec.domain.And.class)
        .extracting("spec")
        .isInstanceOf(Not.class)
        .extracting("spec")
        .isInstanceOf(After.class);
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, mary);

    verify(simpleResolver, times(numberOfLocalField(ForceAnd.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("Force And 2 - @And 宣告在第一順位")
  @Test
  void forceAnd2() {
    var matt =
        repository.save(
            Customer.builder().name("matt").gender(Gender.MALE).birthday(LocalDate.now()).build());
    repository.save(
        Customer.builder()
            .name("bob")
            .gender(Gender.MALE)
            .birthday(LocalDate.now().plusDays(1))
            .build());
    var mary =
        repository.save(
            Customer.builder()
                .name("mary")
                .gender(Gender.FEMALE)
                .birthday(LocalDate.now().minusDays(1))
                .build());

    var criteria =
        ForceAnd2.builder()
            .name(matt.getName())
            .gender(mary.getGender())
            .birthday(LocalDate.now())
            .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    var depth1 =
        assertThat(spec)
            .isNotNull()
            .isInstanceOf(Disjunction.class)
            .extracting("specs", LIST)
            .hasSize(3);
    depth1
        .first()
        .isInstanceOf(tw.com.softleader.data.jpa.spec.domain.And.class)
        .extracting("spec")
        .isInstanceOf(Not.class)
        .extracting("spec")
        .isInstanceOf(After.class);
    depth1.element(1).isInstanceOf(Equals.class);
    depth1.element(2).isInstanceOf(Equals.class);
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, mary);

    verify(simpleResolver, times(numberOfLocalField(ForceAnd2.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("@And 無論宣告在哪個順位都得到相同的結果")
  @Test
  void forceAndIsPermutationInvariant() {
    repository.save(
        Customer.builder().name("matt").gender(Gender.MALE).birthday(LocalDate.now()).build());
    repository.save(
        Customer.builder()
            .name("bob")
            .gender(Gender.MALE)
            .birthday(LocalDate.now().plusDays(1))
            .build());
    repository.save(
        Customer.builder()
            .name("mary")
            .gender(Gender.FEMALE)
            .birthday(LocalDate.now().minusDays(1))
            .build());

    var birthday = LocalDate.now();
    var expected =
        repository.findAll(
            mapper.toSpec(
                ForceAnd.builder().name("matt").gender(Gender.FEMALE).birthday(birthday).build(),
                Customer.class));
    assertThat(expected).isNotEmpty();
    assertThat(
            repository.findAll(
                mapper.toSpec(
                    ForceAnd2.builder()
                        .name("matt")
                        .gender(Gender.FEMALE)
                        .birthday(birthday)
                        .build(),
                    Customer.class)))
        .containsExactlyInAnyOrderElementsOf(expected);
    assertThat(
            repository.findAll(
                mapper.toSpec(
                    ForceAnd3.builder()
                        .name("matt")
                        .gender(Gender.FEMALE)
                        .birthday(birthday)
                        .build(),
                    Customer.class)))
        .containsExactlyInAnyOrderElementsOf(expected);
  }

  @DisplayName("同一個欄位同時標註 @And 及 @Or 時拋出例外")
  @Test
  void andOrAreMutuallyExclusive() {
    var criteria = AndOrConflict.builder().name("matt").build();
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> mapper.toSpec(criteria, Customer.class))
        .withMessageContaining("@And and @Or are mutually exclusive")
        .withMessageContaining(AndOrConflict.class.getName() + ".name");
  }

  int numberOfLocalField(@NonNull Class<?> clazz) {
    var i = new AtomicInteger();
    doWithLocalFields(clazz, f -> i.getAndIncrement());
    return i.intValue();
  }

  @DisplayName("Skip if empty text")
  @Test
  void skipIfEmptyText() {
    var matt =
        repository.save(
            Customer.builder().name("matt").gender(Gender.MALE).birthday(LocalDate.now()).build());
    var bob =
        repository.save(
            Customer.builder()
                .name("bob")
                .gender(Gender.MALE)
                .birthday(LocalDate.now().plusDays(1))
                .build());
    var mary =
        repository.save(
            Customer.builder()
                .name("mary")
                .gender(Gender.FEMALE)
                .birthday(LocalDate.now().minusDays(1))
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
        Customer.builder()
            .name("bob")
            .gender(Gender.MALE)
            .birthday(LocalDate.now().plusDays(1))
            .build());
    repository.save(
        Customer.builder()
            .name("mary")
            .gender(Gender.FEMALE)
            .birthday(LocalDate.now().minusDays(1))
            .build());

    var criteria = SkipEmptyText.builder().name(" ").build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec)
        .isNotNull()
        .isInstanceOf(Conjunction.class)
        .extracting("specs", LIST)
        .hasSize(1)
        .first()
        .isInstanceOf(Equals.class);
    var actual = repository.findAll(spec);
    assertThat(actual).isEmpty();

    verify(simpleResolver, times(numberOfLocalField(SkipEmptyText.class)))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @Builder
  @Data
  public static class MyCriteria {

    @Spec String name;

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

    @Spec String name;

    @Spec Gender gender;

    @Or
    @Spec(value = After.class, not = true)
    LocalDate birthday;
  }

  @Builder
  @Data
  public static class ForceOr2 {

    @Spec String name;

    @Or
    @Spec(value = After.class, not = true)
    LocalDate birthday;

    @Spec Gender gender;
  }

  @Builder
  @Data
  public static class ForceOr3 {

    @Or
    @Spec(value = After.class, not = true)
    LocalDate birthday;

    @Spec String name;

    @Spec Gender gender;
  }

  @Or
  @Builder
  @Data
  public static class ForceAnd {

    @Spec String name;

    @Spec Gender gender;

    @And
    @Spec(value = After.class, not = true)
    LocalDate birthday;
  }

  @Or
  @Builder
  @Data
  public static class ForceAnd2 {

    @And
    @Spec(value = After.class, not = true)
    LocalDate birthday;

    @Spec String name;

    @Spec Gender gender;
  }

  @Or
  @Builder
  @Data
  public static class ForceAnd3 {

    @Spec String name;

    @And
    @Spec(value = After.class, not = true)
    LocalDate birthday;

    @Spec Gender gender;
  }

  @Builder
  @Data
  public static class AndOrConflict {

    @And @Or @Spec String name;
  }

  @Builder
  @Data
  public static class SkipEmptyText {

    @Spec String name;
  }
}
