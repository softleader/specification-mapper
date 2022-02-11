package tw.com.softleader.data.jpa.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.In;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;

@Transactional
@IntegrationTest
class SimpleSpecificationResolverTest {

  @Autowired
  CustomerRepository repository;

  SpecMapper mapper;
  SimpleSpecificationResolver simpleResolver;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder()
        .resolver(simpleResolver = spy(SimpleSpecificationResolver.class))
        .build();
  }

  @DisplayName("空的 @Spec")
  @Test
  void emptySpec() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = MyCriteria.builder().name(matt.getName()).build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);

    verify(simpleResolver, times(3))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("空的 Criteria")
  @Test
  void emptyCriteria() {
    var criteria = MyCriteria.builder().build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNull();
  }

  @DisplayName("Optional Empty")
  @Test
  void optionalEmpty() {
    var criteria = MyCriteria.builder()
        .opt(Optional.empty())
        .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNull();
  }

  @DisplayName("Optional Present")
  @Test
  void optionalPresent() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = MyCriteria.builder()
        .opt(Optional.of("matt"))
        .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);

    verify(simpleResolver, times(3))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @DisplayName("Iterable Empty")
  @Test
  void iterableEmpty() {
    var criteria = MyCriteria.builder()
        .names(List.of())
        .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNull();
  }

  @DisplayName("Iterable Present")
  @Test
  void iterablePresent() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = MyCriteria.builder()
        .names(List.of("matt"))
        .build();
    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);

    verify(simpleResolver, times(3))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @Builder
  @Data
  public static class MyCriteria {

    @Spec
    String name;

    @Spec(path = "name")
    Optional<String> opt;

    @Spec(path = "name", value = In.class)
    Collection<String> names;
  }
}
