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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
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
class BetweenTest {

  @Autowired CustomerRepository repository;

  @Test
  void test() {
    var matt = repository.save(Customer.builder().name("matt").birthday(LocalDate.now()).build());
    repository.save(Customer.builder().name("matt").birthday(LocalDate.now().minusDays(2)).build());

    var spec =
        new Between<Customer>(
            noopContext(),
            "birthday",
            Arrays.asList(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)));
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @Test
  void oneElementThroughMapper() {
    var mapper = SpecMapper.builder().build();
    var birthday = List.of(LocalDate.now());
    var criteria = BetweenCriteria.builder().birthday(birthday).build();
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> mapper.toSpec(criteria, Customer.class))
        .withMessage("@Between expected exact 2 elements, but was " + birthday);
  }

  @Builder
  @Data
  static class BetweenCriteria {

    @Spec(value = Between.class)
    List<LocalDate> birthday;
  }
}
