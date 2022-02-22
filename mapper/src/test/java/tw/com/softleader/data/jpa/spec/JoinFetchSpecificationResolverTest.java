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

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.annotation.JoinFetch;
import tw.com.softleader.data.jpa.spec.annotation.JoinFetch.JoinFetches;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.Order;
import tw.com.softleader.data.jpa.spec.usecase.Tag;

@Transactional
@IntegrationTest
class JoinFetchSpecificationResolverTest {

  @Autowired
  CustomerRepository repository;

  SpecMapper mapper;
  JoinFetchSpecificationResolver joinFetchResolver;
  SimpleSpecificationResolver simpleResolver;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder()
        .resolver(joinFetchResolver = spy(new JoinFetchSpecificationResolver()))
        .resolver(simpleResolver = spy(new SimpleSpecificationResolver()))
        .build();
  }

  @DisplayName("單一層級的 Join Fetch")
  @Test
  void joinFetch() {
    var matt = repository.save(Customer.builder().name("matt")
        .order(Order.builder()
            .itemName("Pizza")
            .build())
        .build());
    repository.save(Customer.builder().name("mary")
        .order(Order.builder()
            .itemName("Hamburger")
            .build())
        .build());
    repository.save(Customer.builder().name("bob")
        .order(Order.builder()
            .itemName("Coke")
            .build())
        .build());

    var spec = mapper.toSpec(new CustomerOrder(matt.getName()), Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @DisplayName("多層級的 Join Fetch")
  @Test
  void joinFetches() {
    var matt = repository.save(Customer.builder().name("matt")
        .order(Order.builder()
            .itemName("Pizza").tag(Tag.builder()
                .name("Food")
                .build())
            .build())
        .build());
    repository.save(Customer.builder().name("mary")
        .order(Order.builder()
            .itemName("Hamburger")
            .tag(Tag.builder()
                .name("Food")
                .build())
            .build())
        .build());
    repository.save(Customer.builder().name("bob")
        .order(Order.builder()
            .itemName("Coke")
            .tag(Tag.builder()
                .name("Beverage")
                .build())
            .build())
        .build());

    var spec = mapper.toSpec(new CustomerOrderTag(matt.getName()), Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @JoinFetch(paths = "orders")
  @AllArgsConstructor
  @Data
  public static class CustomerOrder {

    @Spec
    String name;
  }

  @JoinFetches({
      @JoinFetch(paths = "orders"),
      @JoinFetch(paths = "orders.tags")
  })
  @Data
  @AllArgsConstructor
  public static class CustomerOrderTag {

    @Spec
    String name;
  }

}
