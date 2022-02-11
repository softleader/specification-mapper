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
class NullTest {

  @Autowired
  CustomerRepository repository;

  @Test
  void isNull() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").birthday(LocalDate.now()).build());

    var spec = new Null<Customer>(noopContext(), "birthday", true);
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @Test
  void isNotNull() {
    var matt = repository.save(Customer.builder().name("matt").birthday(LocalDate.now()).build());
    repository.save(Customer.builder().name("bob").build());

    var spec = new Null<Customer>(noopContext(), "birthday", false);
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @Test
  void typeMismatch() {
    var context = noopContext();
    assertThatExceptionOfType(TypeMismatchException.class)
        .isThrownBy(() -> new Null<Customer>(context, "name", "true"))
        .withMessage(
            "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Boolean'");
  }
}
