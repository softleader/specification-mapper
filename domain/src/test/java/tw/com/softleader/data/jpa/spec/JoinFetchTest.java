package tw.com.softleader.data.jpa.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

import java.lang.reflect.Field;
import lombok.Builder;
import lombok.Data;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.annotation.JoinFetch;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.usecase.Badge;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;

@Transactional
@IntegrationTest
class JoinFetchTest {

  @Autowired
  CustomerRepository repository;

  SpecMapper mapper;
  JoinFetchSpecificationResolver joinFetchResolver;
  SimpleSpecificationResolver simpleResolver;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder()
        .resolver(joinFetchResolver = spy(new JoinFetchSpecificationResolver()))
        .resolver(simpleResolver = spy(new SimpleSpecificationResolver()))
        .build();
  }

  @Test
  void fetchesLazyCollection() {
    var matt = repository.save(Customer.builder().name("matt")
        .badge(Badge.builder()
            .badgeType("Ya")
            .build())
        .build());
    repository.save(Customer.builder().name("mary")
        .badge(Badge.builder()
            .badgeType("Ya")
            .build())
        .build());
    repository.save(Customer.builder().name("bob")
        .badge(Badge.builder()
            .badgeType("Oh")
            .build())
        .build());

    var criteria = MyCriteriaWithTwoFetches.builder().hello(matt.getName()).build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);

    for (Customer customer : actual) {
      assertTrue(Hibernate.isInitialized(customer.getBadges()));
    }

    var inOrder = inOrder(
        joinFetchResolver,
        simpleResolver);
    inOrder.verify(joinFetchResolver, times(1))
        .buildSpecification(any(Context.class), any(), any(Field.class));
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(), any(Field.class));
  }

  @Test
  public void performsTwoFetchesUsingSingleLeftJoinFetchDefinition() {
    var matt = repository.save(Customer.builder().name("matt")
        .badge(Badge.builder()
            .badgeType("Ya")
            .build())
        .build());
    repository.save(Customer.builder().name("mary")
        .badge(Badge.builder()
            .badgeType("Ya")
            .build())
        .build());
    repository.save(Customer.builder().name("bob")
        .badge(Badge.builder()
            .badgeType("Oh")
            .build())
        .build());

    var criteria = MyCriteriaWithTwoFetches.builder().hello(matt.getName()).build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);

    for (Customer customer : actual) {
      assertTrue(Hibernate.isInitialized(customer.getBadges()));
      assertTrue(Hibernate.isInitialized(customer.getOrders()));
    }

    var inOrder = inOrder(
        joinFetchResolver,
        simpleResolver);
    inOrder.verify(joinFetchResolver, times(1))
        .buildSpecification(any(Context.class), any(), any(Field.class));
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(), any(Field.class));
  }

  @Builder
  @Data
  @JoinFetch(paths = "badges")
  public static class MyCriteria {

    @Spec(path = "name")
    String hello;
  }

  @Builder
  @Data
  @JoinFetch(paths = { "badges", "orders" })
  public static class MyCriteriaWithTwoFetches {

    @Spec(path = "name")
    String hello;
  }
}
