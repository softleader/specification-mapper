package tw.com.softleader.data.jpa.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;

@Transactional
@IntegrationTest
class CustomizeResolverTest {

  @Autowired
  CustomerRepository repository;

  SpecMapper mapper;

  //  @Test
  void test() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = EmptySpecTest.MyCriteria.builder().name(matt.getName()).build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);

  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.FIELD })
  public @interface MySubquery {

  }

  @Builder
  @Data
  public static class MyCriteria {

    @MySubquery
    String orders;
  }

  public static class MySubqueryResolver implements SpecificationResolver {

    @Override
    public boolean supports(Databind databind) {
      return false;
    }

    @Override
    public Specification<Object> buildSpecification(
        Context context, Databind databind) {
      return null;
    }
  }
}
