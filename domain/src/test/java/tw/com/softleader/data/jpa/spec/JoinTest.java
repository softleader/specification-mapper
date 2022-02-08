package tw.com.softleader.data.jpa.spec;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.annotation.Join;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.usecase.Badge;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.Order;

@Transactional
@Rollback
@IntegrationTest
class JoinTest {

  SpecMapper mapper;

  @Autowired
  CustomerRepository repository;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder()
        .resolver(JoinSpecificationResolver::new)
        .resolver(SimpleSpecificationResolver::new)
        .build();
    repository.deleteAll();
  }

  @Test
  void joinsCollection() {
    var badgeType = "Ya";
    var orderType = "Yo";
    var matt = repository.save(Customer.builder().name("matt")
        .badge(Badge.builder()
            .badgeType(badgeType)
            .build())
        .order(Order.builder()
            .orderType(orderType)
            .build())
        .build());
    var mary = repository.save(Customer.builder().name("mary")
        .badge(Badge.builder()
            .badgeType(badgeType)
            .build())
        .order(Order.builder()
            .orderType(orderType)
            .build())
        .build());
    repository.save(Customer.builder().name("bob")
        .badge(Badge.builder()
            .badgeType("Oh")
            .build())
        .build());

    var criteria = MyCriteria.builder().hello(badgeType).orders(orderType).build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, mary);
  }

  @Builder
  @Data
  public static class MyCriteria {

    @Join(path = "badges", alias = "b")
    @Spec(path = "b.badgeType")
    String hello;

    @Join
    @Spec(path = "orders.orderType")
    String orders;
  }

}
