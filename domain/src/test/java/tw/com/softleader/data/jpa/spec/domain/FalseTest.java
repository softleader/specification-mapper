package tw.com.softleader.data.jpa.spec.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static tw.com.softleader.data.jpa.spec.IntegrationTest.TestApplication.noopContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.IntegrationTest;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;

@Transactional
@IntegrationTest
class FalseTest {

  @Autowired
  CustomerRepository repository;

  @Test
  void isTrue() {
    var matt = repository.save(Customer.builder().name("matt").goldObj(false).build());
    repository.save(Customer.builder().name("bob").goldObj(true).build());
    repository.save(Customer.builder().name("mary").build());

    var spec = new False<Customer>(noopContext(), "goldObj", true);
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @Test
  void isFalse() {
    var matt = repository.save(Customer.builder().name("matt").goldObj(true).build());
    repository.save(Customer.builder().name("bob").goldObj(false).build());
    repository.save(Customer.builder().name("mary").build());

    var spec = new False<Customer>(noopContext(), "goldObj", false);
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);
  }

  @Test
  void typeMismatch() {
    assertThatExceptionOfType(TypeMismatchException.class)
        .isThrownBy(() -> new False<Customer>(noopContext(), "name", "true"))
        .withMessage(
            "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Boolean'");
  }
}
