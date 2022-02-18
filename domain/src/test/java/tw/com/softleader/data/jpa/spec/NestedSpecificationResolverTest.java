/*
 * Copyright © 2022 SoftLeader (support@softleader.com.tw)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tw.com.softleader.data.jpa.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.annotation.And;
import tw.com.softleader.data.jpa.spec.annotation.NestedSpec;
import tw.com.softleader.data.jpa.spec.annotation.Or;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.After;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.StartingWith;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.Gender;

@Transactional
@IntegrationTest
class NestedSpecificationResolverTest {

  @Autowired
  CustomerRepository repository;

  SpecMapper mapper;
  SimpleSpecificationResolver simpleResolver;
  NestedSpecificationResolver compositionResolver;

  Customer matt;
  Customer bob;
  Customer mary;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder()
        .resolver(codec -> compositionResolver = spy(new NestedSpecificationResolver(codec)))
        .resolver(simpleResolver = spy(SimpleSpecificationResolver.class))
        .build();

    matt = repository.save(Customer.builder()
        .name("matt")
        .gold(true)
        .gender(Gender.MALE)
        .birthday(LocalDate.now())
        .build());
    bob = repository.save(Customer.builder().name("bob")
        .gold(false)
        .gender(Gender.MALE)
        .birthday(LocalDate.now().plusDays(1))
        .build());
    mary = repository.save(Customer.builder().name("mary")
        .gold(true)
        .gender(Gender.FEMALE)
        .birthday(LocalDate.now())
        .build());

    // Data will be:
    // matt, gold,     male,   today is birthday
    // bob,  not gold, male,   tomorrow is birthday
    // mary, gold,     female, today is birthday
  }

  @DisplayName("全部都用 AND 組合多個條件")
  @Test
  void allAnd() {
    var criteria = AllAnd.builder()
        .name(matt.getName())
        .nestedAnd(new NestedAnd(matt.getName(), new NestedInNestedAnd(matt.getName())))
        .build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);

    var inOrder = inOrder(
        compositionResolver,
        simpleResolver);
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
    inOrder.verify(compositionResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("全部都用 OR 組合多個條件")
  @Test
  void allOr() {
    var criteria = AllOr.builder()
        .name(matt.getName())
        .nestedOr(
            new NestedOr(bob.getName(), new NestedInNestedOr(mary.getName())))
        .build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(3).contains(matt, bob, mary);

    var inOrder = inOrder(
        compositionResolver,
        simpleResolver);
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
    inOrder.verify(compositionResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("混合 AND 或 OR 組合多個條件")
  @Test
  void mix() {
    var criteria = Mix.builder()
        .name(matt.getName())
        .nestedMix(
            NestedMix.builder()
                .gender(bob.getGender())
                .birthday(bob.getBirthday())
                .nin(
                    NestedInNestedMix.builder()
                        .gold(mary.isGold())
                        .gender(bob.getGender())
                        .build())
                .build())
        .build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);

    var inOrder = inOrder(
        compositionResolver,
        simpleResolver);
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
    inOrder.verify(compositionResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("強制使用 Or 串連")
  @Test
  void forceOr() {
    var criteria = ForceOr.builder()
        .name("m")
        .gold(true)
        .nestedOr(NestedForceOr.builder()
            .birthday(LocalDate.now())
            .gender(Gender.MALE)
            .build())
        .build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(3).contains(matt, mary, bob);

    var inOrder = inOrder(
        compositionResolver,
        simpleResolver);
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
    inOrder.verify(compositionResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("強制使用 And 串連")
  @Test
  void forceAnd() {
    var criteria = ForceAnd.builder()
        .name("m")
        .gold(true)
        .nestedAnd(NestedForceAnd.builder()
            .birthday(LocalDate.now())
            .gender(Gender.MALE)
            .build())
        .build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);

    var inOrder = inOrder(
        compositionResolver,
        simpleResolver);
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
    inOrder.verify(compositionResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @Or
  @Builder
  @Data
  public static class ForceAnd {

    @Spec(StartingWith.class)
    String name;

    @Spec
    Boolean gold;

    @And
    @NestedSpec
    NestedForceAnd nestedAnd;
  }

  @Or
  @Builder
  @Data
  public static class NestedForceAnd {

    @Spec(After.class)
    LocalDate birthday;

    @Spec
    Gender gender;
  }

  @And
  @Builder
  @Data
  public static class ForceOr {

    @Spec(StartingWith.class)
    String name;

    @Spec
    Boolean gold;

    @Or
    @NestedSpec
    NestedForceOr nestedOr;
  }

  @And
  @Builder
  @Data
  public static class NestedForceOr {

    @Spec(After.class)
    LocalDate birthday;

    @Spec
    Gender gender;
  }

  @Builder
  @Data
  public static class AllAnd {

    @Spec
    String name;

    @NestedSpec
    NestedAnd nestedAnd;
  }

  @And
  @AllArgsConstructor
  @Data
  public static class NestedAnd {

    @Spec
    String name;

    @NestedSpec
    NestedInNestedAnd nin;
  }

  @And
  @Data
  @AllArgsConstructor
  public static class NestedInNestedAnd {

    @NestedSpec
    String name;
  }

  @Builder
  @Data
  @Or
  public static class AllOr {

    @Spec
    String name;

    @NestedSpec
    NestedOr nestedOr;
  }

  @Or
  @AllArgsConstructor
  @Data
  public static class NestedOr {

    @Spec
    String name;

    @NestedSpec
    NestedInNestedOr nin;
  }

  @Or
  @Data
  @AllArgsConstructor
  public static class NestedInNestedOr {

    @Spec
    String name;
  }

  @Builder
  @Data
  public static class Mix {

    @Spec
    String name;

    @NestedSpec
    NestedMix nestedMix;
  }

  @Or
  @Builder
  @Data
  public static class NestedMix {

    @Spec
    Gender gender;

    @Spec
    LocalDate birthday;

    @NestedSpec
    NestedInNestedMix nin;
  }

  @And
  @Builder
  @Data
  public static class NestedInNestedMix {

    @Spec
    boolean gold;

    @Spec
    Gender gender;
  }

}
