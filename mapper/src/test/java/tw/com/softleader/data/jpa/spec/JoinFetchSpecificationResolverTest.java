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
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.mockito.Mockito.spy;

import jakarta.persistence.EntityManager;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tw.com.softleader.data.jpa.spec.annotation.JoinFetch;
import tw.com.softleader.data.jpa.spec.annotation.JoinFetch.JoinFetches;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Equals;
import tw.com.softleader.data.jpa.spec.domain.In;
import tw.com.softleader.data.jpa.spec.domain.Like;
import tw.com.softleader.data.jpa.spec.usecase.*;

@IntegrationTest
class JoinFetchSpecificationResolverTest {

  @Autowired CustomerRepository repository;
  @Autowired EntityManager entityManager;

  SpecMapper mapper;
  JoinFetchSpecificationResolver joinFetchResolver;
  SimpleSpecificationResolver simpleResolver;

  @BeforeEach
  void setup() {
    mapper =
        SpecMapper.builder()
            .resolver(joinFetchResolver = spy(new JoinFetchSpecificationResolver()))
            .resolver(simpleResolver = spy(new SimpleSpecificationResolver()))
            .build();
  }

  @DisplayName("JoinFetch 一層在 class 上")
  @Test
  void singleJoinFetchOnClass() {
    var matt =
        repository.save(
            Customer.builder()
                .name("matt")
                .order(Order.builder().itemName("Pizza").build())
                .build());
    repository.save(
        Customer.builder()
            .name("mary")
            .order(Order.builder().itemName("Hamburger").build())
            .build());
    repository.save(
        Customer.builder().name("bob").order(Order.builder().itemName("Coke").build()).build());

    var spec = mapper.toSpec(new SingleJoinFetchOnClass(matt.getName()), Customer.class);
    assertThat(spec)
        .isNotNull()
        .isInstanceOf(Conjunction.class)
        .extracting("specs", LIST)
        .hasSize(2)
        .hasExactlyElementsOfTypes(
            tw.com.softleader.data.jpa.spec.domain.JoinFetch.class, Equals.class);
    assertThat(repository.findAll(spec)).hasSize(1).contains(matt);
    assertThat(repository.count(spec)).isEqualTo(1);
    assertThat(repository.exists(spec)).isTrue();
  }

  @DisplayName("JoinFetch 一層在 field 上")
  @Test
  void singleJoinFetchOnField() {
    var matt =
        repository.save(
            Customer.builder()
                .name("matt")
                .order(Order.builder().itemName("Pizza").build())
                .build());
    var mary =
        repository.save(
            Customer.builder()
                .name("mary")
                .order(Order.builder().itemName("Hamburger").build())
                .build());
    repository.save(
        Customer.builder().name("bob").order(Order.builder().itemName("Coke").build()).build());

    var criteria = SingleJoinFetchOnField.builder().item("Pizza").item("Hamburger").build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec)
        .isNotNull()
        .isInstanceOf(Conjunction.class)
        .extracting("specs", LIST)
        .hasSize(2)
        .hasExactlyElementsOfTypes(
            tw.com.softleader.data.jpa.spec.domain.JoinFetch.class, In.class);
    assertThat(repository.findAll(spec)).hasSize(2).contains(matt, mary);
    assertThat(repository.count(spec)).isEqualTo(2);
    assertThat(repository.exists(spec)).isTrue();
  }

  @DisplayName("JoinFetch 多層在 class 上")
  @Test
  void singleJoinFetchesOnClass() {
    var matt =
        repository.save(
            Customer.builder()
                .name("matt")
                .order(
                    Order.builder()
                        .itemName("Pizza")
                        .tag(Tag.builder().name("Food").build())
                        .build())
                .build());
    repository.save(
        Customer.builder()
            .name("mary")
            .order(
                Order.builder()
                    .itemName("Hamburger")
                    .tag(Tag.builder().name("Food").build())
                    .build())
            .build());
    repository.save(
        Customer.builder()
            .name("bob")
            .order(
                Order.builder()
                    .itemName("Coke")
                    .tag(Tag.builder().name("Beverage").build())
                    .build())
            .build());

    var spec = mapper.toSpec(new MultiJoinFetchesOnClass(matt.getName()), Customer.class);
    var depth1 =
        assertThat(spec)
            .isNotNull()
            .isInstanceOf(Conjunction.class)
            .extracting("specs", LIST)
            .hasSize(2);
    depth1
        .first()
        .isInstanceOf(Conjunction.class)
        .extracting("specs", LIST)
        .hasSize(2)
        .hasOnlyElementsOfType(tw.com.softleader.data.jpa.spec.domain.JoinFetch.class);
    depth1.element(1).isInstanceOf(Equals.class);
    assertThat(repository.findAll(spec)).hasSize(1).contains(matt);
    assertThat(repository.count(spec)).isEqualTo(1);
    assertThat(repository.exists(spec)).isTrue();
  }

  @DisplayName("JoinFetch 多層在 field 上")
  @Test
  void singleJoinFetchesOnField() {
    var matt =
        repository.save(
            Customer.builder()
                .name("matt")
                .order(
                    Order.builder()
                        .itemName("Pizza")
                        .tag(Tag.builder().name("Food").build())
                        .build())
                .build());
    var mary =
        repository.save(
            Customer.builder()
                .name("mary")
                .order(
                    Order.builder()
                        .itemName("Hamburger")
                        .tag(Tag.builder().name("Food").build())
                        .build())
                .build());
    repository.save(
        Customer.builder()
            .name("bob")
            .order(
                Order.builder()
                    .itemName("Coke")
                    .tag(Tag.builder().name("Beverage").build())
                    .build())
            .build());

    var criteria = MultiJoinFetchesOnField.builder().tag("Food").build();

    var spec = mapper.toSpec(criteria, Customer.class);
    var depth1 =
        assertThat(spec)
            .isNotNull()
            .isInstanceOf(Conjunction.class)
            .extracting("specs", LIST)
            .hasSize(2);
    depth1
        .first()
        .isInstanceOf(Conjunction.class)
        .extracting("specs", LIST)
        .hasSize(2)
        .hasOnlyElementsOfType(tw.com.softleader.data.jpa.spec.domain.JoinFetch.class);
    depth1.element(1).isInstanceOf(In.class);
    assertThat(repository.findAll(spec)).hasSize(2).contains(matt, mary);
    assertThat(repository.count(spec)).isEqualTo(2);
    assertThat(repository.exists(spec)).isTrue();
  }

  @DisplayName("JoinFetch 多層在 class 上, 且物件無任何 fields")
  @Test
  void multiJoinFetchesOnClassOnly() {
    var matt =
        repository.save(
            Customer.builder()
                .name("matt")
                .order(
                    Order.builder()
                        .itemName("Pizza")
                        .tag(Tag.builder().name("Food").build())
                        .build())
                .build());
    var mary =
        repository.save(
            Customer.builder()
                .name("mary")
                .order(
                    Order.builder()
                        .itemName("Hamburger")
                        .tag(Tag.builder().name("Food").build())
                        .build())
                .build());
    repository.save(
        Customer.builder()
            .name("bob")
            .order(
                Order.builder()
                    .itemName("Coke")
                    .tag(Tag.builder().name("Beverage").build())
                    .build())
            .build());

    var criteria = new MultiJoinFetchesOnClassOnly();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec)
        .isNotNull()
        .isInstanceOf(Conjunction.class)
        .extracting("specs", LIST)
        .hasSize(1)
        .first()
        .isInstanceOf(Conjunction.class)
        .extracting("specs", LIST)
        .hasSize(2)
        .hasOnlyElementsOfType(tw.com.softleader.data.jpa.spec.domain.JoinFetch.class);
    assertThat(repository.findAll(spec)).hasSize(3).contains(matt, mary);
    assertThat(repository.count(spec)).isEqualTo(3);
    assertThat(repository.exists(spec)).isTrue();
  }

  @DisplayName("JoinFetch 多層在 class 上, 被多個 fields 所使用")
  @Test
  void multiJoinFetchesOnClassUsedByMultiFields() {
    var matt =
        repository.save(
            Customer.builder()
                .name("matt")
                .order(Order.builder().itemName("Pizza").build())
                .school(School.builder().name("A").build())
                .build());
    repository.save(
        Customer.builder()
            .name("mary")
            .order(Order.builder().itemName("Hamburger").build())
            .school(School.builder().name("A").build())
            .build());
    repository.save(
        Customer.builder()
            .name("bob")
            .order(Order.builder().itemName("Coke").build())
            .school(School.builder().name("B").build())
            .build());

    var spec =
        mapper.toSpec(
            MultiJoinFetchesOnClassUsedByMultiFields.builder()
                .itemName("Pizza")
                .schoolName("A")
                .build(),
            Customer.class);
    var depth1 =
        assertThat(spec)
            .isNotNull()
            .isInstanceOf(Conjunction.class)
            .extracting("specs", LIST)
            .hasSize(3);
    depth1
        .first()
        .isInstanceOf(Conjunction.class)
        .extracting("specs", LIST)
        .hasSize(2)
        .hasOnlyElementsOfType(tw.com.softleader.data.jpa.spec.domain.JoinFetch.class);
    depth1.element(1).isInstanceOf(Equals.class);
    depth1.element(2).isInstanceOf(Equals.class);
    assertThat(repository.findAll(spec)).hasSize(1).contains(matt);
    assertThat(repository.count(spec)).isEqualTo(1);
    assertThat(repository.exists(spec)).isTrue();
  }

  @DisplayName("相同 alias 的 JoinFetch 不應該重複產生")
  @Test
  @SuppressWarnings("DataFlowIssue")
  void duplicateAliasJoinFetchOnField() {

    var criteria = DuplicateAliasJoinFetchOnField.builder().orderId(1L).itemName("Pizza").build();

    var spec = mapper.toSpec(criteria, Customer.class);

    var cb = entityManager.getCriteriaBuilder();
    var query = cb.createQuery(Customer.class);
    var root = query.from(Customer.class);

    spec.toPredicate(root, query, cb);

    repository.findAll(spec);

    assertThat(root.getFetches()).hasSize(1);
  }

  @JoinFetch(path = "orders")
  @AllArgsConstructor
  @Data
  public static class SingleJoinFetchOnClass {

    @Spec String name;
  }

  @JoinFetches({@JoinFetch(path = "orders"), @JoinFetch(path = "orders.tags")})
  @Data
  @AllArgsConstructor
  public static class MultiJoinFetchesOnClass {

    @Spec String name;
  }

  @Builder
  @Data
  public static class SingleJoinFetchOnField {

    @Singular
    @JoinFetch(path = "orders", alias = "o")
    @Spec(path = "o.itemName", value = In.class)
    Collection<String> items;
  }

  @Builder
  @Data
  public static class MultiJoinFetchesOnField {

    @Singular
    @JoinFetch(path = "orders", alias = "o")
    @Spec(path = "o.itemName", value = In.class)
    Collection<String> items;

    @Singular
    @JoinFetches({@JoinFetch(path = "orders"), @JoinFetch(path = "orders.tags")})
    @Spec(path = "orders_tags.name", value = In.class)
    Collection<String> tags;
  }

  @JoinFetches({@JoinFetch(path = "orders"), @JoinFetch(path = "schools")})
  @Builder
  @Data
  public static class MultiJoinFetchesOnClassUsedByMultiFields {
    @Spec(path = "orders.itemName")
    String itemName;

    @Spec(path = "schools.name")
    String schoolName;
  }

  @JoinFetch(path = "orders", alias = "o")
  @JoinFetch(path = "o.tags", alias = "t")
  public static class MultiJoinFetchesOnClassOnly {}

  @Builder
  @Data
  public static class DuplicateAliasJoinFetchOnField {

    @JoinFetch(path = "orders", alias = "order")
    @Spec(path = "order.id")
    Long orderId;

    @JoinFetch(path = "orders", alias = "order")
    @Spec(path = "order.itemName", value = Like.class)
    String itemName;
  }
}
