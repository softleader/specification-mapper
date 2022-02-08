package tw.com.softleader.data.jpa.spec;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.annotation.Spec;

@SpringBootTest
@AutoConfigureDataJpa
@SpringBootApplication
@Transactional
class E2eTest {

  @Autowired
  CustomerRepository repository;

  @Test
  void simpleCase() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = MyCriteria.builder().hello(matt.getName()).build();
    var actual = repository.findBySpec(criteria);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @Test
  void emptyCriteria() {
    var matt = repository.save(Customer.builder().name("matt").build());
    var bob = repository.save(Customer.builder().name("bob").build());
    var mary = repository.save(Customer.builder().name("mary").build());

    var criteria = MyCriteria.builder().build();
    var actual = repository.findBySpec(criteria);
    assertThat(actual).hasSize(3).contains(matt, bob, mary);
  }

  @Builder
  @Data
  public static class MyCriteria {

    @Spec(path = "name")
    String hello;
  }

}
