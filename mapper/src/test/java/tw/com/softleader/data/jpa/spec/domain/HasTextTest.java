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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tw.com.softleader.data.jpa.spec.IntegrationTest;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;

@IntegrationTest
class HasTextTest {

  @Autowired CustomerRepository repository;

  @Test
  void hasText() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("  ").birthday(LocalDate.now()).build());

    var spec = new HasText<Customer>(noopContext(), "name", true);
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @Test
  void notHasText() {
    repository.save(Customer.builder().name("matt").build());
    var empty = repository.save(Customer.builder().name("  ").birthday(LocalDate.now()).build());

    var spec = new HasText<Customer>(noopContext(), "name", false);
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(empty);
  }

  @Test
  void typeMismatch() {
    var context = noopContext();
    assertThatExceptionOfType(TypeMismatchException.class)
        .isThrownBy(() -> new HasText<Customer>(context, "name", "true"))
        .withMessage(
            "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Boolean'");
  }
}
