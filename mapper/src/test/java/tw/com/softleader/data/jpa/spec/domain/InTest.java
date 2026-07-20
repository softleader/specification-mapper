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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static tw.com.softleader.data.jpa.spec.IntegrationTest.TestApplication.noopContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tw.com.softleader.data.jpa.spec.IntegrationTest;
import tw.com.softleader.data.jpa.spec.SpecMapper;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;

@IntegrationTest
class InTest {

  @Autowired CustomerRepository repository;

  @Test
  void test() {
    var matt = repository.save(Customer.builder().name("matt").build());
    var bob = repository.save(Customer.builder().name("bob").build());
    repository.save(Customer.builder().name("mary").build());

    var spec = new In<Customer>(noopContext(), "name", Arrays.asList("matt", "bob"));
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, bob);
  }

  @Test
  void moreValuesThanChunkSize() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var values = new ArrayList<String>();
    IntStream.rangeClosed(1, In.MAX_CHUNK_SIZE).mapToObj(i -> "name-" + i).forEach(values::add);
    values.add("matt");

    var spec = new In<Customer>(noopContext(), "name", values);
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @Test
  void typeMismatch() {
    var context = noopContext();
    var value = new Object();
    assertThatExceptionOfType(TypeMismatchException.class)
        .isThrownBy(() -> new In<Customer>(context, "name", value))
        .withMessage(
            "Failed to convert value of type 'java.lang.Object' to required type 'java.lang.Iterable'");
  }

  @Test
  void typeMismatchThroughMapper() {
    var mapper = SpecMapper.builder().build();
    var criteria = InCriteria.builder().name("matt").build();
    assertThatExceptionOfType(TypeMismatchException.class)
        .isThrownBy(() -> mapper.toSpec(criteria, Customer.class))
        .withMessage(
            "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Iterable'");
  }

  @Builder
  @Data
  static class InCriteria {

    @Spec(path = "name", value = In.class)
    String name;
  }
}
