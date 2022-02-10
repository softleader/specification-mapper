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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.annotation.And;
import tw.com.softleader.data.jpa.spec.annotation.Or;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.usecase.Customer;
import tw.com.softleader.data.jpa.spec.usecase.CustomerRepository;
import tw.com.softleader.data.jpa.spec.usecase.Gender;

@Transactional
@IntegrationTest
class CompositionSpecificationResolverTest {

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

  @DisplayName("AND 連接多個條件")
  @Test
  void and() {
    var matt = repository.save(Customer.builder().name("matt").build());
    repository.save(Customer.builder().name("bob").build());

    var criteria = CriteriaAnd.builder().hello(matt.getName())
        .nestedAnd(new NestedAnd(matt.getName(), new NestedInNested(matt.getName())))
        .build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(1).contains(matt);

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

  @DisplayName("OR 連接多個條件")
  @Test
  void or() {
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

    var criteria = CriteriaOr.builder()
        .hello(matt.getName())
        .nestedOr(
            new NestedOr(bob.getGender(), mary.isGold(), new NestedInNested(matt.getName())))
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
  public static class CriteriaAnd {

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

    NestedInNested nin;
  }

  @Builder
  @Data
  @Or
  public static class CriteriaOr {

    @Spec(path = "name")
    String hello;

    @Or
    NestedOr nestedOr;
  }

  @AllArgsConstructor
  @Data
  public static class NestedOr {

    @Spec(path = "gender")
    Gender hello;

    @Spec(path = "gold")
    boolean aaa;

    @Or
    NestedInNested nin;
  }

  @Data
  @AllArgsConstructor
  public static class NestedInNested {

    @Spec
    String name;
  }
}
