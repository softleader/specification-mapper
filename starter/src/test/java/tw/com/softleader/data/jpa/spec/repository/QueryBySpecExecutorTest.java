package tw.com.softleader.data.jpa.spec.repository;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.repository.usecase.Customer;
import tw.com.softleader.data.jpa.spec.repository.usecase.CustomerRepository;

@Transactional
@SpringBootTest
@AutoConfigureDataJpa
@SpringBootApplication
class QueryBySpecExecutorTest {

  @Autowired
  CustomerRepository repository;

  @Test
  void findBySpec() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = MyCriteria.builder().hello(matt.getName()).build();
    var actual = repository.findBySpec(criteria);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @Test
  void findByEmptySpec() {
    var matt = repository.save(Customer.builder().name("matt").build());
    var bob = repository.save(Customer.builder().name("bob").build());
    var mary = repository.save(Customer.builder().name("mary").build());

    var criteria = MyCriteria.builder().build();
    var actual = repository.findBySpec(criteria);
    assertThat(actual).hasSize(3).contains(matt, bob, mary);
  }

  @Test
  void findBySpecAndSort() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = MyCriteria.builder().hello(matt.getName()).build();
    var actual = repository.findBySpec(criteria, Sort.by("name"));
    assertThat(actual).hasSize(1).contains(matt);
  }

  @Test
  void findBySpecAndPageable() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = MyCriteria.builder().hello(matt.getName()).build();
    var actual = repository.findBySpec(criteria, Pageable.unpaged());
    assertThat(actual.getContent()).hasSize(1).contains(matt);
  }

  @Test
  void countBySpec() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = MyCriteria.builder().hello(matt.getName()).build();
    var actual = repository.countBySpec(criteria);
    assertThat(actual).isEqualTo(1L);
  }

  @Test
  void existBySpec() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = MyCriteria.builder().hello(matt.getName()).build();
    var actual = repository.existsBySpec(criteria);
    assertThat(actual).isTrue();
  }

  @Builder
  @Data
  public static class MyCriteria {

    @Spec(path = "name")
    String hello;
  }

}
