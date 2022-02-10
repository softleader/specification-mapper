package tw.com.softleader.data.jpa.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.annotation.Or;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.Gender;

@Transactional
@IntegrationTest
class OrTest {

  @Autowired
  CustomerRepository repository;

  SpecMapper mapper;
  SimpleSpecificationResolver simpleResolver;
  CompositionSpecificationResolver compositionResolver;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder()
        .resolver(codec -> compositionResolver = spy(new CompositionSpecificationResolver(codec)))
        .resolver(simpleResolver = spy(SimpleSpecificationResolver.class))
        .build();
  }

  @Test
  void nestedOr() {
    var matt = repository.save(Customer.builder()
        .name("matt")
        .gold(true)
        .gender(Gender.MALE)
        .build());
    var bob = repository.save(Customer.builder().name("bob")
        .gold(false)
        .gender(Gender.MALE)
        .build());
    var mary = repository.save(Customer.builder().name("mary")
        .gold(true)
        .gender(Gender.FEMALE)
        .build());

    var criteria = MyCriteria.builder()
        .hello(matt.getName())
        .nestedOr(new NestedOr(bob.getGender(), mary.isGold(), new NestedOr2nd(matt.getName())))
        .build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(3).contains(matt, bob, mary);

    var inOrder = inOrder(
        compositionResolver,
        simpleResolver);
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
    inOrder.verify(compositionResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
    inOrder.verify(simpleResolver, times(1))
        .buildSpecification(any(Context.class), any(Databind.class));
  }

  @Builder
  @Data
  @Or
  public static class MyCriteria {

    @Spec(path = "name")
    String hello;

    @Or
    NestedOr nestedOr;
  }

  @Data
  @AllArgsConstructor
  public static class NestedOr {

    @Spec(path = "gender")
    Gender hello;

    @Spec(path = "gold")
    boolean aaa;

    @Or
    NestedOr2nd nestedOr2nd;
  }

  @Data
  @AllArgsConstructor
  public static class NestedOr2nd {

    @Spec
    String name;
  }
}
