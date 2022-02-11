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
class InTest {

  @Autowired
  CustomerRepository repository;

  @Test
  void test() {
    var matt = repository.save(Customer.builder().name("matt").build());
    var bob = repository.save(Customer.builder().name("bob").build());
    repository.save(Customer.builder().name("mary").build());

    var spec = new In<Customer>(noopContext(), "name", List.of("matt", "bob"));
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, bob);
  }

  @Test
  void typeMismatch() {
    var context = noopContext();
    var value = new Object();
    assertThatExceptionOfType(TypeMismatchException.class)
        .isThrownBy(() -> new In<Customer>(context, "name", value))
        .withMessage(
            "Failed to convert value of type 'java.lang.Object' to required type 'java.lang.Iterable'");
  }
}
