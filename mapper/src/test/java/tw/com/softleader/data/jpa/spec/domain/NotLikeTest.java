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
import static tw.com.softleader.data.jpa.spec.IntegrationTest.TestApplication.noopContext;

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
class NotLikeTest {

  @Autowired CustomerRepository repository;

  @Test
  void test() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var context = noopContext();
    var spec = new NotLike<Customer>(context, "name", "o");
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @Test
  void wildcardsMatchLiterally() {
    repository.save(Customer.builder().name("a%b").build());
    var underscore = repository.save(Customer.builder().name("a_b").build());
    var backslash = repository.save(Customer.builder().name("a\\b").build());
    var plain = repository.save(Customer.builder().name("axb").build());

    var mapper = SpecMapper.builder().build();
    var spec = mapper.toSpec(NotLikeCriteria.builder().name("a%b").build(), Customer.class);
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(3).contains(underscore, backslash, plain);
  }

  @Builder
  @Data
  static class NotLikeCriteria {

    @Spec(path = "name", value = NotLike.class)
    String name;
  }
}
