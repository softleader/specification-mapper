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
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tw.com.softleader.data.jpa.spec.annotation.Join;
import tw.com.softleader.data.jpa.spec.annotation.Join.Joins;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.In;
import tw.com.softleader.data.jpa.spec.domain.Like;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.Order;
import tw.com.softleader.data.jpa.spec.usecase.Tag;

@IntegrationTest
class JoinSpecificationResolverTest {

  @Autowired CustomerRepository repository;

  @Autowired EntityManager entityManager;

  SpecMapper mapper;
  JoinSpecificationResolver joinResolver;
  SimpleSpecificationResolver simpleResolver;

  @BeforeEach
  void setup() {
    mapper =
        SpecMapper.builder()
            .resolver(joinResolver = spy(new JoinSpecificationResolver()))
            .resolver(simpleResolver = spy(new SimpleSpecificationResolver()))
            .build();
  }

  @DisplayName("Join 一層在 field 上")
  @Test
  void singleJoinOnField() {
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

    var criteria = SingleJoinOnField.builder().item("Pizza").item("Hamburger").build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec)
        .isNotNull()
        .isInstanceOf(Conjunction.class)
        .extracting("specs", LIST)
        .hasSize(2)
        .hasExactlyElementsOfTypes(tw.com.softleader.data.jpa.spec.domain.Join.class, In.class);
    assertThat(repository.findAll(spec)).hasSize(2).contains(matt, mary);
    assertThat(repository.count(spec)).isEqualTo(2);
    assertThat(repository.exists(spec)).isTrue();
  }

  @DisplayName("Join 一層 在 class 上")
  @Test
  void singleJoinOnClass() {
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

    var criteria = SingleJoinOnClass.builder().item("Pizza").item("Hamburger").build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec)
        .isNotNull()
        .isInstanceOf(Conjunction.class)
        .extracting("specs", LIST)
        .hasSize(2)
        .hasExactlyElementsOfTypes(tw.com.softleader.data.jpa.spec.domain.Join.class, In.class);
    assertThat(repository.findAll(spec)).hasSize(2).contains(matt, mary);
    assertThat(repository.count(spec)).isEqualTo(2);
    assertThat(repository.exists(spec)).isTrue();
  }

  @DisplayName("Join 多層在 field 上")
  @Test
  void multiJoinsOnField() {
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

    var criteria = MultiJoinsOnField.builder().tag("Food").build();

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
        .hasOnlyElementsOfType(tw.com.softleader.data.jpa.spec.domain.Join.class);
    depth1.element(1).isInstanceOf(In.class);
    assertThat(repository.findAll(spec)).hasSize(2).contains(matt, mary);
    assertThat(repository.count(spec)).isEqualTo(2);
    assertThat(repository.exists(spec)).isTrue();
  }

  @DisplayName("Join 多層在 class 上")
  @Test
  void multiJoinsOnClass() {
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

    var criteria = MultiJoinsOnClass.builder().tag("Food").build();

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
        .hasOnlyElementsOfType(tw.com.softleader.data.jpa.spec.domain.Join.class);
    depth1.element(1).isInstanceOf(In.class);
    assertThat(repository.findAll(spec)).hasSize(2).contains(matt, mary);
    assertThat(repository.count(spec)).isEqualTo(2);
    assertThat(repository.exists(spec)).isTrue();
  }

  @DisplayName("Join 多層在 class 上, 且物件無任何 fields")
  @Test
  void multiJoinsOnClassOnly() {
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

    var criteria = new MultiJoinsOnClassOnly();

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
        .hasOnlyElementsOfType(tw.com.softleader.data.jpa.spec.domain.Join.class);
    assertThat(repository.findAll(spec)).hasSize(3).contains(matt, mary);
    assertThat(repository.count(spec)).isEqualTo(3);
    assertThat(repository.exists(spec)).isTrue();
  }

  @DisplayName("相同 alias 的 Join 不應該重複產生")
  @Test
  @SuppressWarnings("DataFlowIssue")
  void duplicateAliasJoinOnField() {

    var criteria = DuplicateAliasJoinOnField.builder().orderId(1L).itemName("Pizza").build();

    var spec = mapper.toSpec(criteria, Customer.class);

    var cb = entityManager.getCriteriaBuilder();
    var query = cb.createQuery(Customer.class);
    var root = query.from(Customer.class);

    spec.toPredicate(root, query, cb);

    repository.findAll(spec);

    assertThat(root.getJoins()).hasSize(1);
  }

  @Builder
  @Data
  public static class SingleJoinOnField {

    @Singular
    @Join(path = "orders", alias = "o")
    @Spec(path = "o.itemName", value = In.class)
    Collection<String> items;
  }

  @Builder
  @Data
  @Join(path = "orders")
  public static class SingleJoinOnClass {

    @Singular
    @Spec(path = "orders.itemName", value = In.class)
    Collection<String> items;
  }

  @Builder
  @Data
  public static class MultiJoinsOnField {

    @Singular
    @Joins({@Join(path = "orders"), @Join(path = "orders.tags")})
    @Spec(path = "orders_tags.name", value = In.class)
    Collection<String> tags;
  }

  @Builder
  @Data
  @Joins({@Join(path = "orders", alias = "o"), @Join(path = "o.tags", alias = "t")})
  public static class MultiJoinsOnClass {

    @Singular
    @Spec(path = "o.itemName", value = In.class)
    Collection<String> items;

    @Singular
    @Spec(path = "t.name", value = In.class)
    Collection<String> tags;
  }

  @Join(path = "orders", alias = "o")
  @Join(path = "o.tags", alias = "t")
  public static class MultiJoinsOnClassOnly {}

  @Builder
  @Data
  public static class DuplicateAliasJoinOnField {

    @Join(path = "orders", alias = "order")
    @Spec(path = "order.id")
    Long orderId;

    @Join(path = "orders", alias = "order")
    @Spec(path = "order.itemName", value = Like.class)
    String itemName;
  }
}
