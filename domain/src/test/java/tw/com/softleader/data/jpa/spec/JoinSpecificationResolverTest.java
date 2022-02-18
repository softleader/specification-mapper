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
package tw.com.softleader.data.jpa.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import java.util.Collection;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.annotation.Join;
import tw.com.softleader.data.jpa.spec.annotation.Join.Joins;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.In;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.Order;
import tw.com.softleader.data.jpa.spec.usecase.Tag;

@Transactional
@IntegrationTest
class JoinSpecificationResolverTest {

  @Autowired
  CustomerRepository repository;

  SpecMapper mapper;
  JoinSpecificationResolver joinResolver;
  SimpleSpecificationResolver simpleResolver;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder()
        .resolver(joinResolver = spy(new JoinSpecificationResolver()))
        .resolver(simpleResolver = spy(new SimpleSpecificationResolver()))
        .build();
  }

  @DisplayName("單一層級的 Join")
  @Test
  void join() {
    var matt = repository.save(Customer.builder().name("matt")
        .order(Order.builder()
            .itemName("Pizza")
            .build())
        .build());
    var mary = repository.save(Customer.builder().name("mary")
        .order(Order.builder()
            .itemName("Hamburger")
            .build())
        .build());
    repository.save(Customer.builder().name("bob")
        .order(Order.builder()
            .itemName("Coke")
            .build())
        .build());

    var criteria = CustomerOrder.builder()
        .item("Pizza")
        .item("Hamburger")
        .build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, mary);
  }

  @DisplayName("多層級的 Join")
  @Test
  void joins() {
    var matt = repository.save(Customer.builder().name("matt")
        .order(Order.builder()
            .itemName("Pizza").tag(Tag.builder()
                .name("Food")
                .build())
            .build())
        .build());
    var mary = repository.save(Customer.builder().name("mary")
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

    var criteria = CustomerOrder.builder()
        .tag("Food")
        .build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, mary);
  }

  @Builder
  @Data
  public static class CustomerOrder {

    @Singular
    @Join(path = "orders", alias = "o")
    @Spec(path = "o.itemName", value = In.class)
    Collection<String> items;

    @Singular
    @Joins({
        @Join(path = "orders", alias = "o"),
        @Join(path = "o.tags", alias = "t")
    })
    @Spec(path = "t.name", value = In.class)
    Collection<String> tags;
  }

}
