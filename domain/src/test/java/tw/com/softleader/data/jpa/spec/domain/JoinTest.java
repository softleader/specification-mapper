package tw.com.softleader.data.jpa.spec.domain;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.Builder;
import lombok.Data;
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
import tw.com.softleader.data.jpa.spec.annotation.Join;
import tw.com.softleader.data.jpa.spec.annotation.Spec;

@Transactional
@Rollback
@IntegrationTest
class JoinTest {

  SpecMapper mapper;

  @Autowired
  CustomerRepository repository;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder().build();
    repository.deleteAll();
  }

  @Test
  void joinsCollection() {
    var badgeType = "Ya";
    var matt = repository.save(Customer.builder().name("matt")
        .badge(Badge.builder()
            .badgeType(badgeType)
            .build())
        .build());
    var mary = repository.save(Customer.builder().name("mary")
        .badge(Badge.builder()
            .badgeType(badgeType)
            .build())
        .build());
    repository.save(Customer.builder().name("bob")
        .badge(Badge.builder()
            .badgeType("Oh")
            .build())
        .build());

    var criteria = MyCriteria.builder().hello(badgeType).build();

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
  }

}
