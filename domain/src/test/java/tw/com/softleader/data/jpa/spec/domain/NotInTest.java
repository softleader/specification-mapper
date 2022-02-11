package tw.com.softleader.data.jpa.spec.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static tw.com.softleader.data.jpa.spec.IntegrationTest.TestApplication.noopContext;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.IntegrationTest;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;

@Transactional
@IntegrationTest
class NotInTest {

  @Autowired
  CustomerRepository repository;

  @Test
  void test() {
    repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());
    var mary = repository.save(Customer.builder().name("mary").build());

    var spec = new NotIn<Customer>(noopContext(), "name", List.of("matt", "bob"));
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(mary);
  }

  @Test
  void typeMismatch() {
    assertThatExceptionOfType(TypeMismatchException.class)
        .isThrownBy(() -> new NotIn<Customer>(noopContext(), "name", new Object()))
        .withMessage(
            "Failed to convert value of type 'java.lang.Object' to required type 'java.lang.Iterable'");
  }
}
