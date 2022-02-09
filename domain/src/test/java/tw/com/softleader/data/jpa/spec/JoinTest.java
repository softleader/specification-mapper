package tw.com.softleader.data.jpa.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.annotation.Join;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.usecase.Badge;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.Order;

@Transactional
@IntegrationTest
class JoinTest {

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
