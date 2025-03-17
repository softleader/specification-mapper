package tw.com.softleader.data.jpa.spec;

import static java.lang.Integer.toHexString;
import static java.lang.System.identityHashCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static tw.com.softleader.data.jpa.spec.SpecJoinContext.HandleKey.identityHex;

import java.lang.annotation.Annotation;
import org.junit.jupiter.api.Test;
import tw.com.softleader.data.jpa.spec.SpecJoinContext.HandleKey;

class SpecJoinContextTest {

  @Test
  void shouldConvertTargetToIdentityHexString() {
    var target = new Object();
    var annotation = mock(Annotation.class);

    var key = new HandleKey(target, null, annotation);

    var expectedTarget = identityHex(target);
    assertThat(key.target()).isEqualTo(expectedTarget);
    assertThat(key.field()).isNull();
    assertThat(key.def()).isEqualTo(annotation);
  }

  @Test
  void shouldConvertFieldToIdentityHexString() throws NoSuchFieldException {
    var target = new Object();
    var annotation = mock(Annotation.class);
    var field = TargetA.class.getDeclaredField("field");

    var key = new HandleKey(target, field, annotation);

    var expectedField = toHexString(identityHashCode(field));
    assertThat(key.target()).isEqualTo(toHexString(identityHashCode(target)));
    assertThat(key.field()).isEqualTo(expectedField);
    assertThat(key.def()).isEqualTo(annotation);
  }

  @Test
  void shouldHandleNullField() {
    var target = new Object();
    var annotation = mock(Annotation.class);

    var key = new HandleKey(target, null, annotation);

    assertThat(key.target()).isEqualTo(toHexString(identityHashCode(target)));
    assertThat(key.field()).isNull();
    assertThat(key.def()).isEqualTo(annotation);
  }

  @Test
  void shouldGenerateDifferentKeysForSameFieldNameInDifferentClasses() throws NoSuchFieldException {
    var target = new Object();
    var annotation = mock(Annotation.class);

    var fieldA = TargetA.class.getDeclaredField("field");
    var fieldB = TargetB.class.getDeclaredField("field");

    var keyA = new HandleKey(target, fieldA, annotation);
    var keyB = new HandleKey(target, fieldB, annotation);

    assertThat(keyA).isNotEqualTo(keyB);
    assertThat(keyA.field()).isNotEqualTo(keyB.field());
  }

  @Test
  void shouldGenerateDifferentKeysForSameTypeFieldsInSameClass() throws NoSuchFieldException {
    var target = new Object();
    var annotation = mock(Annotation.class);

    var field1 = TargetC.class.getDeclaredField("fieldA");
    var field2 = TargetC.class.getDeclaredField("fieldB");

    var key1 = new HandleKey(target, field1, annotation);
    var key2 = new HandleKey(target, field2, annotation);

    assertThat(key1).isNotEqualTo(key2);
    assertThat(key1.field()).isNotEqualTo(key2.field());
  }

  static class TargetA {
    private String field;
  }

  static class TargetB {
    private String field;
  }

  static class TargetC {
    private String fieldA;
    private String fieldB;
  }
}
