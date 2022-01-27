package tw.com.softleader.data.jpa.spec.domain;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.Customer;
import tw.com.softleader.data.jpa.spec.CustomerRepository;
import tw.com.softleader.data.jpa.spec.IntegrationTest;
import tw.com.softleader.data.jpa.spec.SpecMapper;
import tw.com.softleader.data.jpa.spec.bind.annotation.Or;
import tw.com.softleader.data.jpa.spec.bind.annotation.Spec;

@Transactional
@Rollback
@IntegrationTest
class OrTest {

  SpecMapper mapper;

  @Autowired
  CustomerRepository repository;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder().build();
    repository.deleteAll();
  }

  @Test
  void test() {
    var matt = repository.save(Customer.builder().name("matt").build());
    var bob = repository.save(Customer.builder().name("bob").build());
    repository.save(Customer.builder().name("mary").build());

    var criteria = MyCriteria.builder().hello(matt.getName())
        .nestedOr(new NestedOr(bob.getName()))
        .build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, bob);
  }

  @Builder
  @Data
  public static class MyCriteria {

    @Or
    @Spec(path = "name", spec = Equal.class)
    String hello;

    @Or
    NestedOr nestedOr;
  }

  @AllArgsConstructor
  public static class NestedOr {

    @Spec(path = "name", spec = Equal.class)
    String hello;
  }
}
