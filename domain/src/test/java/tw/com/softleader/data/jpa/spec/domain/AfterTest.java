/*
 * Copyright © 2022 SoftLeader (support@softleader.com.tw)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tw.com.softleader.data.jpa.spec.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static tw.com.softleader.data.jpa.spec.IntegrationTest.TestApplication.noopContext;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.IntegrationTest;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;

@Transactional
@IntegrationTest
class AfterTest {

  @Autowired
  CustomerRepository repository;

  @Test
  void test() {
    var matt = repository.save(Customer.builder().name("matt").birthday(LocalDate.now()).build());
    repository.save(Customer.builder().name("matt").birthday(LocalDate.now().minusDays(1)).build());

    var spec = new After<Customer>(noopContext(), "birthday", LocalDate.now().minusDays(1));
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }
}
