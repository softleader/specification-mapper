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
package tw.com.softleader.data.jpa.spec.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static tw.com.softleader.data.jpa.spec.IntegrationTest.TestApplication.noopContext;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.IntegrationTest;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.Gender;

@IntegrationTest
class DisjunctionTest {

  @Autowired CustomerRepository repository;

  Customer matt;
  Customer bob;
  Customer mary;

  Specification<Customer> nameIsBob;
  Specification<Customer> genderIsMale;
  Specification<Customer> nameIsMary;

  @BeforeEach
  void setup() {
    matt = repository.save(Customer.builder().name("matt").gender(Gender.MALE).build());
    bob = repository.save(Customer.builder().name("bob").gender(Gender.MALE).build());
    mary = repository.save(Customer.builder().name("mary").gender(Gender.FEMALE).build());

    nameIsBob = new Equals<>(noopContext(), "name", "bob");
    genderIsMale = new Equals<>(noopContext(), "gender", Gender.MALE);
    nameIsMary = new Equals<>(noopContext(), "name", "mary");
  }

  @DisplayName("combine 沒有 Wrapper 的元素時使用 Or")
  @Test
  void combineWithoutWrapper() {
    var disjunction = new Disjunction<Customer>(List.of());
    var combined = disjunction.combine(genderIsMale, nameIsMary);
    assertThat(repository.findAll(combined)).containsExactlyInAnyOrder(matt, bob, mary);
  }

  @DisplayName("combine 有 And Wrapper 的元素時使用 And")
  @Test
  void combineWithAndWrapper() {
    var disjunction = new Disjunction<Customer>(List.of());
    var combined = disjunction.combine(genderIsMale, new And<>(nameIsBob));
    assertThat(repository.findAll(combined)).containsExactly(bob);
  }

  @DisplayName("overridesOperator 只認得 And")
  @Test
  void overridesOperator() {
    var disjunction = new Disjunction<Customer>(List.of());
    assertThat(disjunction.overridesOperator(new And<>(nameIsBob))).isTrue();
    assertThat(disjunction.overridesOperator(new Or<>(nameIsBob))).isFalse();
    assertThat(disjunction.overridesOperator(nameIsBob)).isFalse();
  }

  @DisplayName("第一順位的 And 不會被忽略")
  @Test
  void andOnFirstPositionIsNotIgnored() {
    var spec = new Disjunction<>(List.of(new And<>(genderIsMale), nameIsBob));
    assertThat(repository.findAll(spec)).containsExactly(bob);
  }

  @DisplayName("And 在任何順位都得到相同的結果")
  @Test
  void permutationInvariance() {
    var expected = List.of(bob);
    assertThat(repository.findAll(new Disjunction<>(List.of(new And<>(genderIsMale), nameIsBob))))
        .containsExactlyInAnyOrderElementsOf(expected);
    assertThat(repository.findAll(new Disjunction<>(List.of(nameIsBob, new And<>(genderIsMale)))))
        .containsExactlyInAnyOrderElementsOf(expected);
  }

  @DisplayName("三個元素的所有排列都得到相同的結果")
  @Test
  void permutationInvarianceOfThreeElements() {
    Specification<Customer> and = new And<>(genderIsMale);
    var permutations =
        List.of(
            List.of(and, nameIsBob, nameIsMary),
            List.of(and, nameIsMary, nameIsBob),
            List.of(nameIsBob, and, nameIsMary),
            List.of(nameIsBob, nameIsMary, and),
            List.of(nameIsMary, and, nameIsBob),
            List.of(nameIsMary, nameIsBob, and));
    // (name = 'bob' or name = 'mary') and gender = MALE
    var expected = List.of(bob);
    permutations.forEach(
        specs ->
            assertThat(repository.findAll(new Disjunction<>(specs)))
                .as("specs=%s", specs)
                .containsExactlyInAnyOrderElementsOf(expected));
  }

  @DisplayName("全部都是 And 時依然全部以 And 結合")
  @Test
  void allElementsAreAnd() {
    var spec = new Disjunction<>(List.of(new And<>(genderIsMale), new And<>(nameIsBob)));
    assertThat(repository.findAll(spec)).containsExactly(bob);
  }

  @DisplayName("沒有任何元素時不產生 Predicate")
  @Test
  void noElement() {
    assertThat(repository.findAll(new Disjunction<Customer>(List.of())))
        .containsExactlyInAnyOrder(matt, bob, mary);
  }
}
