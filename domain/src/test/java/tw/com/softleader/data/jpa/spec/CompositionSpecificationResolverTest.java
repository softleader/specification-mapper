package tw.com.softleader.data.jpa.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.annotation.And;
import tw.com.softleader.data.jpa.spec.annotation.CompositeSpec;
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

  Customer matt;
  Customer bob;
  Customer mary;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder()
        .resolver(codec -> compositionResolver = spy(new CompositionSpecificationResolver(codec)))
        .resolver(simpleResolver = spy(SimpleSpecificationResolver.class))
        .build();

    matt = repository.save(Customer.builder()
        .name("matt")
        .gold(true)
        .gender(Gender.MALE)
        .birthday(LocalDate.now())
        .build());
    bob = repository.save(Customer.builder().name("bob")
        .gold(false)
        .gender(Gender.MALE)
        .birthday(LocalDate.now().plusDays(1))
        .build());
    mary = repository.save(Customer.builder().name("mary")
        .gold(true)
        .gender(Gender.FEMALE)
        .birthday(LocalDate.now())
        .build());

    // Data will be:
    // matt, gold,     male,   today is birthday
    // bob,  not gold, male,   tomorrow is birthday
    // mary, gold,     female, today is birthday
  }

  @DisplayName("全部都用 AND 組合多個條件")
  @Test
  void allAnd() {
    var criteria = CriteriaAnd.builder()
        .name(matt.getName())
        .nestedAnd(new NestedAnd(matt.getName(), new NestedInNestedAnd(matt.getName())))
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

  @DisplayName("全部都用 OR 組合多個條件")
  @Test
  void allOr() {
    var criteria = CriteriaOr.builder()
        .name(matt.getName())
        .nestedOr(
            new NestedOr(bob.getName(), new NestedInNestedOr(mary.getName())))
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

  @DisplayName("混合 AND 或 OR 組合多個條件")
  @Test
  void mix() {
    var criteria = CriteriaMix.builder()
        .name(matt.getName())
        .nestedMix(
            NestedMix.builder()
                .gender(bob.getGender())
                .birthday(bob.getBirthday())
                .nin(
                    NestedInNestedMix.builder()
                        .gold(mary.isGold())
                        .gender(bob.getGender())
                        .build())
                .build())
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

  @Builder
  @Data
  public static class CriteriaAnd {

    @Spec
    String name;

    @CompositeSpec
    NestedAnd nestedAnd;
  }

  @And
  @AllArgsConstructor
  @Data
  public static class NestedAnd {

    @Spec
    String name;

    @CompositeSpec
    NestedInNestedAnd nin;
  }

  @And
  @Data
  @AllArgsConstructor
  public static class NestedInNestedAnd {

    @CompositeSpec
    String name;
  }

  @Builder
  @Data
  @Or
  public static class CriteriaOr {

    @Spec
    String name;

    @CompositeSpec
    NestedOr nestedOr;
  }

  @Or
  @AllArgsConstructor
  @Data
  public static class NestedOr {

    @Spec
    String name;

    @CompositeSpec
    NestedInNestedOr nin;
  }

  @Or
  @Data
  @AllArgsConstructor
  public static class NestedInNestedOr {

    @Spec
    String name;
  }

  @Builder
  @Data
  public static class CriteriaMix {

    @Spec
    String name;

    @CompositeSpec
    NestedMix nestedMix;
  }

  @Or
  @Builder
  @Data
  public static class NestedMix {

    @Spec
    Gender gender;

    @Spec
    LocalDate birthday;

    @CompositeSpec
    NestedInNestedMix nin;
  }

  @And
  @Builder
  @Data
  public static class NestedInNestedMix {

    @Spec
    boolean gold;

    @Spec
    Gender gender;
  }

}
