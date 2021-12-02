package tw.com.softleader.data.jpa.spec.domain;

import static org.assertj.core.api.Assertions.assertThat;

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
import tw.com.softleader.data.jpa.spec.domain.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.annotation.Spec.Ordered;

@Transactional
@Rollback
@IntegrationTest
class EqualTest {

  SpecMapper mapper;

  @Autowired
  CustomerRepository repository;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder()
      .resolver(new SpecResolver())
      .build();

    repository.deleteAll();

  }

  @Test
  void test() {
    var name = "matt";
    var expected = Customer.builder().name(name).build();
    repository.save(expected);
    repository.save(Customer.builder().name("rhys").build());
    var criteria = MyCriteria.builder().hello(name).build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).first().isEqualTo(expected);
  }

  @Builder
  @Data
  public static class MyCriteria {

    @Spec(path = "name", spec = Equal.class, order = Ordered.HIGHEST_PRECEDENCE)
    String hello;

  }
}
