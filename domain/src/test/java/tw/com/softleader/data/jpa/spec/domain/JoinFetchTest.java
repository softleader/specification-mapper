package tw.com.softleader.data.jpa.spec.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.Builder;
import lombok.Data;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.Badge;
import tw.com.softleader.data.jpa.spec.Customer;
import tw.com.softleader.data.jpa.spec.CustomerRepository;
import tw.com.softleader.data.jpa.spec.IntegrationTest;
import tw.com.softleader.data.jpa.spec.SpecMapper;
import tw.com.softleader.data.jpa.spec.annotation.JoinFetch;
import tw.com.softleader.data.jpa.spec.annotation.Spec;

@Transactional
@Rollback
@IntegrationTest
class JoinFetchTest {

  SpecMapper mapper;

  @Autowired
  CustomerRepository repository;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder().build();
    repository.deleteAll();
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
