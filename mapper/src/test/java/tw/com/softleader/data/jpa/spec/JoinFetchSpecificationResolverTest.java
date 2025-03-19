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
import static org.mockito.Mockito.spy;

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
import tw.com.softleader.data.jpa.spec.domain.In;
import tw.com.softleader.data.jpa.spec.usecase.*;

@IntegrationTest
class JoinFetchSpecificationResolverTest {

  @Autowired CustomerRepository repository;

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

  @DisplayName("單一層級的 Join Fetch 在 class 上, 多個 join")
  @Test
  void multiJoinFetch() {
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
            SingleLevelMultiJoinFetches.builder().itemName("Pizza").schoolName("A").build(),
            Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @DisplayName("單一層級的 Join Fetch 在 class 上")
  @Test
  void joinFetch() {
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

    var spec = mapper.toSpec(new CustomerJoinFetchOnClass(matt.getName()), Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @DisplayName("單一層級的 Join 在 field 上")
  @Test
  void join() {
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

    var criteria = CustomerJoinFetchOnField.builder().item("Pizza").item("Hamburger").build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, mary);
  }

  @DisplayName("多層級的 Join Fetch 在 class 上")
  @Test
  void joinFetches() {
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

    var spec = mapper.toSpec(new CustomerJoinFetchesOnClass(matt.getName()), Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @DisplayName("多層級的 Fetch Join 在 field 上")
  @Test
  void joins() {
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

    var criteria = CustomerJoinFetchOnField.builder().tag("Food").build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, mary);
  }

  @DisplayName("多層級的 Join Fetches 僅在 class 上 (無任何欄位)")
  @Test
  void joinFetchesOnClassOnly() {
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

    var criteria = new CustomerJoinsOnClassOnly();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(3).contains(matt, mary);
  }

  @JoinFetch(path = "orders")
  @AllArgsConstructor
  @Data
  public static class CustomerJoinFetchOnClass {

    @Spec String name;
  }

  @JoinFetches({@JoinFetch(path = "orders"), @JoinFetch(path = "orders.tags")})
  @Data
  @AllArgsConstructor
  public static class CustomerJoinFetchesOnClass {

    @Spec String name;
  }

  @Builder
  @Data
  public static class CustomerJoinFetchOnField {

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
  public static class SingleLevelMultiJoinFetches {
    @Spec(path = "orders.itemName")
    String itemName;

    @Spec(path = "schools.name")
    String schoolName;
  }

  @JoinFetches({@JoinFetch(path = "orders", alias = "o"), @JoinFetch(path = "o.tags", alias = "t")})
  public static class CustomerJoinsOnClassOnly {}
}
