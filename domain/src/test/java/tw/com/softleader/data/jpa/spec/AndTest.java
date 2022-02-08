package tw.com.softleader.data.jpa.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

import java.lang.reflect.Field;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.annotation.And;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;

@Transactional
@Rollback
@IntegrationTest
class AndTest {

  SpecMapper mapper;
  SimpleSpecificationResolver simpleResolver;
  CompositionSpecificationResolver compositionResolver;

  @Autowired
  CustomerRepository repository;

  @BeforeEach
  void setup() {
    simpleResolver = spy(SimpleSpecificationResolver.class);
    mapper = SpecMapper.builder()
        .resolver(codec -> compositionResolver = spy(new CompositionSpecificationResolver(codec)))
        .resolver(simpleResolver)
        .build();
    repository.deleteAll();
  }

  @Test
  void test() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = MyCriteria.builder().hello(matt.getName())
        .nestedAnd(new NestedAnd(matt.getName()))
        .build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);

    var inOrder = Mockito.inOrder(
        compositionResolver,
        simpleResolver);
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(), any(Field.class));
    inOrder.verify(compositionResolver, times(1))
        .buildSpecification(any(Context.class), any(), any(Field.class));
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(), any(Field.class));
  }

  @Builder
  @Data
  public static class MyCriteria {

    @Spec(path = "name")
    String hello;

    @And
    NestedAnd nestedAnd;
  }

  @AllArgsConstructor
  @Data
  public static class NestedAnd {

    @Spec(path = "name")
    String hello;
  }
}
