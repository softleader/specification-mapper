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
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.IntegrationTest;
import tw.com.softleader.data.jpa.spec.SpecMapper;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;

@IntegrationTest
class LikeTest {

  @Autowired CustomerRepository repository;

  @Test
  void test() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var context = noopContext();
    var spec = new Like<Customer>(context, "name", "at");
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @Test
  void wildcardsMatchLiterally() {
    var percent = repository.save(Customer.builder().name("a%b").build());
    var underscore = repository.save(Customer.builder().name("a_b").build());
    var backslash = repository.save(Customer.builder().name("a\\b").build());
    repository.save(Customer.builder().name("axb").build());

    var mapper = SpecMapper.builder().build();
    assertThat(repository.findAll(toSpec(mapper, "a%b"))).hasSize(1).contains(percent);
    assertThat(repository.findAll(toSpec(mapper, "a_b"))).hasSize(1).contains(underscore);
    assertThat(repository.findAll(toSpec(mapper, "a\\b"))).hasSize(1).contains(backslash);
  }

  private Specification<Customer> toSpec(SpecMapper mapper, String name) {
    return mapper.toSpec(LikeCriteria.builder().name(name).build(), Customer.class);
  }

  @Builder
  @Data
  static class LikeCriteria {

    @Spec(path = "name", value = Like.class)
    String name;
  }
}
