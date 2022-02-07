package tw.com.softleader.data.jpa.spec;

import static java.util.Arrays.stream;

import java.lang.reflect.Field;
import java.util.Objects;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.bind.ConjunctionSpecification;
import tw.com.softleader.data.jpa.spec.bind.ConjunctionSpecifications;
import tw.com.softleader.data.jpa.spec.bind.annotation.And;
import tw.com.softleader.data.jpa.spec.bind.annotation.Or;
import tw.com.softleader.data.jpa.spec.bind.annotation.Spec;
import tw.com.softleader.data.jpa.spec.util.FieldUtil;

/**
 * @author Matt Ho
 */
@Slf4j
@Builder
public class SpecMapper implements SpecCodec {

  @Override
  public <T> Specification<T> toSpec(Object obj, Class<T> rootType) {
    return toConjunctionSpecification(obj);
  }

  /**
   * 將 {@code obj} 中的所有欄位轉換成一個 {@code ConjunctionSpecification}
   *
   * @param obj 要轉換的物件
   */
  ConjunctionSpecification toConjunctionSpecification(Object obj) {
    if (obj == null) {
      return null;
    }
    var spec = stream(obj.getClass().getDeclaredFields())
        .map(field -> toConjunctionSpecification(obj, field))
        .filter(Objects::nonNull)
        .peek(System.out::println)
        .reduce(ConjunctionSpecification::composed)
        .orElse(null);
    System.out.println("=== " + spec);
    return spec;
  }

  /**
   * 將 {@code obj} 中的 {@code field} 轉換成一個 {@code ConjunctionSpecification}
   *
   * @param obj 欄位所屬的物件, 供取值用
   * @param field 要轉換成 {@code ConjunctionSpecification} 的欄位
   */
  private ConjunctionSpecification toConjunctionSpecification(Object obj, Field field) {
    if (field.isAnnotationPresent(And.class) || field.isAnnotationPresent(Or.class)) {
      if (field.isAnnotationPresent(Spec.class)) {
        throw new IllegalStateException(
            "Invalid annotation. The declaration of '@Spec' does not applicable with '@And/@Or'");
      }
      log.debug(" -> Going deeper into [{}.{}] ({})",
          obj.getClass().getSimpleName(),
          field.getName(),
          FieldUtil.getReadMethod(obj, field).getReturnType().getName());
      var composed = toConjunctionSpecification(FieldUtil.getValue(obj, field));
      if (field.isAnnotationPresent(Or.class)) {
        composed = ConjunctionSpecifications.or(composed);
      }
      log.debug(" <- Going upper from [{}.{}] and composed: {}",
          obj.getClass().getSimpleName(),
          field.getName(),
          composed);
      return composed;
    }
    return ConjunctionSpecifications.of(obj, field);
  }

}
