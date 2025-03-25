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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tw.com.softleader.data.jpa.spec.annotation.JoinFetch;
import tw.com.softleader.data.jpa.spec.annotation.NestedSpec;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Equals;
import tw.com.softleader.data.jpa.spec.domain.In;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.School;

@IntegrationTest
class JoinFetchElementCollectionTest {

  @Autowired CustomerRepository repository;

  SpecMapper mapper;
  NestedSpecificationResolver nestedResolver;
  JoinFetchSpecificationResolver joinFetchResolver;
  SimpleSpecificationResolver simpleResolver;

  @BeforeEach
  void setup() {
    mapper =
        SpecMapper.builder()
            .resolver(codec -> nestedResolver = spy(new NestedSpecificationResolver(codec)))
            .resolver(joinFetchResolver = spy(new JoinFetchSpecificationResolver()))
            .resolver(spy(new JoinSpecificationResolver()))
            .resolver(simpleResolver = spy(new SimpleSpecificationResolver()))
            .build();
  }

  @DisplayName("@JoinFetch 遇上 @ElementCollection")
  @Test
  void joinFetchWithElementCollection() {
    var matt =
        repository.save(
            Customer.builder()
                .name("matt")
                .phone("taiwanmobile", "0911222333")
                .phone("cht", "0944555666")
                .build());
    repository.save(
        Customer.builder()
            .name("mary")
            .phone("cht", "0955666777")
            .phone("fetnet", "0966777888")
            .build());

    var spec =
        mapper.toSpec(JoinFetchElementCollection.builder().name("matt").build(), Customer.class);
    var depth1 =
        assertThat(spec)
            .isNotNull()
            .isInstanceOf(Conjunction.class)
            .extracting("specs", LIST)
            .hasSize(2);
    depth1.first().isInstanceOf(tw.com.softleader.data.jpa.spec.domain.JoinFetch.class);
    depth1.element(1).isInstanceOf(Equals.class);
    verify(joinFetchResolver, times(1)).buildSpecification(any(), any());
    verify(nestedResolver, never()).buildSpecification(any(), any());
    assertThat(repository.findAll(spec)).hasSize(1).contains(matt);
    assertThat(repository.count(spec)).isEqualTo(1);
    assertThat(repository.exists(spec)).isTrue();
  }

  @DisplayName("巢狀的 @JoinFetch")
  @Test
  void nestedJoinFetch() {
    var matt =
        repository.save(
            Customer.builder()
                .name("matt")
                .phone("taiwanmobile", "0911222333")
                .phone("cht", "0944555666")
                .school(School.builder().city("Taipei").name("A").build())
                .build());
    repository.save(
        Customer.builder()
            .name("mary")
            .phone("cht", "0955666777")
            .phone("fetnet", "0966777888")
            .school(School.builder().city("Taipei").name("B").build())
            .build());
    repository.save(
        Customer.builder()
            .name("bob")
            .phone("cht", "0955666777")
            .phone("taiwanmobile", "0977888999")
            .school(School.builder().city("Taichung").name("B").build())
            .build());
    var criteria =
        NestedJoinFetchElementCollection.builder()
            .school(JoinFetchOnClass.builder().name("A").city("Taipei").build())
            .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    var depth1 =
        assertThat(spec)
            .isNotNull()
            .isInstanceOf(Conjunction.class)
            .extracting("specs", LIST)
            .hasSize(2);
    depth1.first().isInstanceOf(tw.com.softleader.data.jpa.spec.domain.JoinFetch.class);
    var depth2 =
        depth1.element(1).isInstanceOf(Conjunction.class).extracting("specs", LIST).hasSize(3);
    depth2.first().isInstanceOf(tw.com.softleader.data.jpa.spec.domain.JoinFetch.class);
    depth2.element(1).isInstanceOf(Equals.class);
    depth2.element(2).isInstanceOf(In.class);
    verify(joinFetchResolver, times(2)).buildSpecification(any(), any());
    verify(nestedResolver, times(1)).buildSpecification(any(), any());
    assertThat(repository.findAll(spec)).hasSize(1).contains(matt);
    assertThat(repository.count(spec)).isEqualTo(1);
    assertThat(repository.exists(spec)).isTrue();
  }

  @Data
  @Builder
  @JoinFetch(path = "phones", alias = "p")
  public static class JoinFetchElementCollection {
    @Spec String name;
  }

  @Data
  @Builder
  @JoinFetch(path = "phones")
  public static class NestedJoinFetchElementCollection {

    @NestedSpec JoinFetchOnClass school;
  }

  @Data
  @Builder
  @JoinFetch(path = "schools")
  public static class JoinFetchOnClass {

    @Spec(path = "schools.city")
    String city;

    @Singular
    @Spec(path = "schools.name", value = In.class)
    Set<String> names;
  }
}
