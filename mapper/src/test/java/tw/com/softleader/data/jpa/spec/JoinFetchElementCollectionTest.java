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

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.annotation.Join;
import tw.com.softleader.data.jpa.spec.annotation.JoinFetch;
import tw.com.softleader.data.jpa.spec.annotation.NestedSpec;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.In;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.Gender;
import tw.com.softleader.data.jpa.spec.usecase.School;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@IntegrationTest
class JoinFetchElementCollectionTest {

  @Autowired
  CustomerRepository repository;

  SpecMapper mapper;
  NestedSpecificationResolver nestedResolver;
  JoinFetchSpecificationResolver joinFetchResolver;
  SimpleSpecificationResolver simpleResolver;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder()
        .resolver(codec -> nestedResolver = spy(new NestedSpecificationResolver(codec)))
        .resolver(joinFetchResolver = spy(new JoinFetchSpecificationResolver()))
        .resolver(spy(new JoinSpecificationResolver()))
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
    assertThat(spec)
        .isNotNull()
        .extracting("specs", LIST)
        .map(Specification.class::cast)
        .filteredOn(tw.com.softleader.data.jpa.spec.domain.JoinFetch.class::isInstance)
        .hasSize(1);
    val totalFields = CustomerFetchPhone.class.getDeclaredFields().length;
    verify(joinFetchResolver, times(totalFields)).buildSpecification(any(), any());
    verify(nestedResolver, times(1)).buildSpecification(any(), any());
    val actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @DisplayName("巢狀的 @JoinFetch")
  @Test
  void nestedJoinFetch() {
    val matt = repository.save(Customer.builder().name("matt")
        .phone("taiwanmobile", "0911222333")
        .phone("cht", "0944555666")
        .school(School.builder()
            .city("Taipei")
            .name("A")
            .build())
        .build());
    repository.save(Customer.builder().name("mary")
        .phone("cht", "0955666777")
        .phone("fetnet", "0966777888")
        .school(School.builder()
            .city("Taipei")
            .name("B")
            .build())
        .build());
    repository.save(Customer.builder().name("bob")
        .phone("cht", "0955666777")
        .phone("taiwanmobile", "0977888999")
        .school(School.builder()
            .city("Taichung")
            .name("B")
            .build())
        .build());
    val criteria = CustomerFetchPhone.builder()
        .school(CustomerFetchSchool.builder()
            .name("A")
            .city("Taipei")
            .build())
        .build();
    val spec = mapper.toSpec(criteria, Customer.class);
    val specs = assertThat(spec)
        .isNotNull()
        .extracting("specs", LIST)
        .map(Specification.class::cast);
    specs
        .filteredOn(tw.com.softleader.data.jpa.spec.domain.JoinFetch.class::isInstance)
        .hasSize(1);
    specs
        .filteredOn(Conjunction.class::isInstance)
        .hasSize(1)
        .first()
        .extracting("specs", LIST)
        .filteredOn(tw.com.softleader.data.jpa.spec.domain.JoinFetch.class::isInstance)
        .hasSize(1);
    val totalFields = CustomerFetchPhone.class.getDeclaredFields().length
        + CustomerFetchSchool.class.getDeclaredFields().length;
    verify(joinFetchResolver, times(totalFields)).buildSpecification(any(), any());
    verify(nestedResolver, times(1)).buildSpecification(any(), any());
    val actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @Data
  @Builder
  @JoinFetch(paths = "phones")
  public static class CustomerFetchPhone {

    @Spec
    String name;

    @Spec
    Gender gender;

    @NestedSpec
    CustomerFetchSchool school;
  }

  @Data
  @Builder
  @JoinFetch(paths = "schools")
  public static class CustomerFetchSchool {

    @Join(path = "schools", alias = "s")
    @Spec(path = "s.city")
    String city;

    @Singular
    @Join(path = "schools", alias = "s")
    @Spec(path = "s.name", value = In.class)
    Set<String> names;
  }
}
