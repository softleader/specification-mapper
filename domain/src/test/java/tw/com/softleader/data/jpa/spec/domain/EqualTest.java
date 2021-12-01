package tw.com.softleader.data.jpa.spec.domain;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tw.com.softleader.data.jpa.spec.annotation.Or;
import tw.com.softleader.data.jpa.spec.annotation.Spec;

class EqualTest {

  SpecMapper mapper;

  @BeforeEach
  void setup() {
    mapper = new SpecMapper();
  }

  @Test
  void test() {
    var mock = new MockEntity();
    var spec = mapper.toSpec(mock);
    assertThat(spec).isNotNull();
  }

  @Data
  public static class MockCriteria {
    @Spec(path = "root", spec = Equal.class)
    String hello;

    @Or
    XXX xxx;
  }

  public static class XXX {
    @Spec()
    String hello;
  }

  @Data
  public static class MockEntity {
    String name;
  }
}
