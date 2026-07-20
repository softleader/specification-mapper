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
class ConjunctionTest {

  @Autowired CustomerRepository repository;

  Customer matt;
  Customer bob;
  Customer mary;

  Specification<Customer> nameIsBob;
  Specification<Customer> genderIsFemale;
  Specification<Customer> nameIsMary;

  @BeforeEach
  void setup() {
    matt = repository.save(Customer.builder().name("matt").gender(Gender.MALE).build());
    bob = repository.save(Customer.builder().name("bob").gender(Gender.MALE).build());
    mary = repository.save(Customer.builder().name("mary").gender(Gender.FEMALE).build());

    nameIsBob = new Equals<>(noopContext(), "name", "bob");
    genderIsFemale = new Equals<>(noopContext(), "gender", Gender.FEMALE);
    nameIsMary = new Equals<>(noopContext(), "name", "mary");
  }

  @DisplayName("combine 沒有 Wrapper 的元素時使用 And")
  @Test
  void combineWithoutWrapper() {
    var conjunction = new Conjunction<Customer>(List.of());
    var combined = conjunction.combine(genderIsFemale, nameIsBob);
    assertThat(repository.findAll(combined)).isEmpty();
  }

  @DisplayName("combine 有 Or Wrapper 的元素時使用 Or")
  @Test
  void combineWithOrWrapper() {
    var conjunction = new Conjunction<Customer>(List.of());
    var combined = conjunction.combine(genderIsFemale, new Or<>(nameIsBob));
    assertThat(repository.findAll(combined)).containsExactlyInAnyOrder(bob, mary);
  }

  @DisplayName("overridesOperator 只認得 Or")
  @Test
  void overridesOperator() {
    var conjunction = new Conjunction<Customer>(List.of());
    assertThat(conjunction.overridesOperator(new Or<>(nameIsBob))).isTrue();
    assertThat(conjunction.overridesOperator(new And<>(nameIsBob))).isFalse();
    assertThat(conjunction.overridesOperator(nameIsBob)).isFalse();
  }

  @DisplayName("第一順位的 Or 不會被忽略")
  @Test
  void orOnFirstPositionIsNotIgnored() {
    var spec = new Conjunction<>(List.of(new Or<>(nameIsBob), genderIsFemale));
    assertThat(repository.findAll(spec)).containsExactlyInAnyOrder(bob, mary);
  }

  @DisplayName("Or 在任何順位都得到相同的結果")
  @Test
  void permutationInvariance() {
    var expected = List.of(bob, mary);
    assertThat(repository.findAll(new Conjunction<>(List.of(new Or<>(nameIsBob), genderIsFemale))))
        .containsExactlyInAnyOrderElementsOf(expected);
    assertThat(repository.findAll(new Conjunction<>(List.of(genderIsFemale, new Or<>(nameIsBob)))))
        .containsExactlyInAnyOrderElementsOf(expected);
  }

  @DisplayName("三個元素的所有排列都得到相同的結果")
  @Test
  void permutationInvarianceOfThreeElements() {
    Specification<Customer> or = new Or<>(nameIsBob);
    var permutations =
        List.of(
            List.of(or, genderIsFemale, nameIsMary),
            List.of(or, nameIsMary, genderIsFemale),
            List.of(genderIsFemale, or, nameIsMary),
            List.of(genderIsFemale, nameIsMary, or),
            List.of(nameIsMary, or, genderIsFemale),
            List.of(nameIsMary, genderIsFemale, or));
    // (gender = FEMALE and name = 'mary') or name = 'bob'
    var expected = List.of(bob, mary);
    permutations.forEach(
        specs ->
            assertThat(repository.findAll(new Conjunction<>(specs)))
                .as("specs=%s", specs)
                .containsExactlyInAnyOrderElementsOf(expected));
  }

  @DisplayName("全部都是 Or 時依然全部以 Or 結合")
  @Test
  void allElementsAreOr() {
    var spec = new Conjunction<>(List.of(new Or<>(nameIsBob), new Or<>(nameIsMary)));
    assertThat(repository.findAll(spec)).containsExactlyInAnyOrder(bob, mary);
  }

  @DisplayName("沒有任何元素時不產生 Predicate")
  @Test
  void noElement() {
    assertThat(repository.findAll(new Conjunction<Customer>(List.of())))
        .containsExactlyInAnyOrder(matt, bob, mary);
  }
}
