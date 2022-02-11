package tw.com.softleader.data.jpa.spec.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static tw.com.softleader.data.jpa.spec.IntegrationTest.TestApplication.noopContext;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.IntegrationTest;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;

@Transactional
@IntegrationTest
class GreaterThanEqualTest {

  @Autowired
  CustomerRepository repository;

  @Test
  void test() {
    var matt = repository.save(Customer.builder().name("matt").birthday(LocalDate.now()).build());
    repository.save(Customer.builder().name("matt").birthday(LocalDate.now().minusDays(1)).build());

    var spec = new GreaterThanEqual<Customer>(noopContext(), "birthday", LocalDate.now());
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @Test
  void typeMismatch() {
    var context = noopContext();
    var value = new Object();
    assertThatExceptionOfType(TypeMismatchException.class)
        .isThrownBy(() -> new GreaterThanEqual<Customer>(context, "name", value))
        .withMessage(
            "Failed to convert value of type 'java.lang.Object' to required type 'java.lang.Comparable'");
  }
}
