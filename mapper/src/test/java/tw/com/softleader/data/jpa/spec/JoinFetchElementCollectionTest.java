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

import lombok.Builder;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tw.com.softleader.data.jpa.spec.JoinFetchSpecificationResolverTest.CustomerOrder;
import tw.com.softleader.data.jpa.spec.annotation.JoinFetch;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;

@IntegrationTest
class JoinFetchElementCollectionTest {

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

  @DisplayName("@JoinFetch 遇上 @ElementCollection")
  @Test
  void joinFetchWithElementCollection() {
    val matt = repository.save(Customer.builder().name("matt")
        .phone("taiwanmobile", "0911222333")
        .phone("cht", "0944555666")
        .build());
    repository.save(Customer.builder().name("mary")
        .phone("cht", "0955666777")
        .phone("fetnet", "0966777888")
        .build());

    val spec = mapper.toSpec(
        CustomerFetchPhone.builder()
            .name("matt")
            .build(),
        Customer.class);
    assertThat(spec).isNotNull();
    val actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @Data
  @Builder
  @JoinFetch(paths = "phones")
  public static class CustomerFetchPhone {

    @Spec
    String name;
  }
}
