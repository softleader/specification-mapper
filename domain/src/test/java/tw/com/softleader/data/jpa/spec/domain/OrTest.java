package tw.com.softleader.data.jpa.spec.domain;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.Customer;
import tw.com.softleader.data.jpa.spec.CustomerRepository;
import tw.com.softleader.data.jpa.spec.Gender;
import tw.com.softleader.data.jpa.spec.IntegrationTest;
import tw.com.softleader.data.jpa.spec.SpecMapper;
import tw.com.softleader.data.jpa.spec.bind.Compose;
import tw.com.softleader.data.jpa.spec.bind.annotation.Or;
import tw.com.softleader.data.jpa.spec.bind.annotation.Spec;

@Transactional
@Rollback
@IntegrationTest
class OrTest {

  SpecMapper mapper;

  @Autowired
  CustomerRepository repository;

  @BeforeEach
  void setup() {
    mapper = SpecMapper.builder().build();
    repository.deleteAll();
  }

  @Test
  void test() {
    var matt = repository.save(Customer.builder()
        .name("matt")
        .gold(true)
        .gender(Gender.MALE)
        .build());
    var bob = repository.save(Customer.builder().name("bob")
        .gold(true)
        .gender(Gender.MALE)
        .build());
    var mary = repository.save(Customer.builder().name("mary")
        .gold(true)
        .gender(Gender.FEMALE)
        .build());

    var criteria = MyCriteria.builder()
        .hello(matt.getName())
        .nestedOr(new NestedOr(bob.getGender(), mary.isGold()))
        .build();

    var spec = mapper.toSpec(criteria, Customer.class);
    assertThat(spec).isNotNull();
    var actual = repository.findAll(spec);
    assertThat(actual).hasSize(2).contains(matt, bob);
  }

  @Builder
  @Data
  public static class MyCriteria {

    @Spec(path = "name", spec = Equal.class)
    String hello;

    @Or
    NestedOr nestedOr;
  }

  @Data
  @AllArgsConstructor
  public static class NestedOr {

    @Spec(path = "gender", spec = Equal.class)
    Gender hello;

    @Spec(path = "gold", spec = Equal.class, compose = Compose.OR)
    boolean aaa;
  }
}
