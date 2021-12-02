package tw.com.softleader.data.jpa.spec.domain;

import static java.util.Arrays.stream;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.SneakyThrows;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.domain.annotation.Spec;

/**
 * @author Matt Ho
 */
@Builder
public class SpecMapper implements SpecCodec {

  @Singular
  List<SpecificationResolver> resolvers;

  @Override
  public <T> Specification<T> toSpec(Object obj, Class<T> rootType) {
    return stream(obj.getClass().getDeclaredFields())
      .map(field -> toSpec(obj, field, rootType))
      .reduce((left, right) -> left.and(right)) // and !!
      .get(); // TODO: remove get and cast
  }

  @SneakyThrows
  private <T> Specification<T> toSpec(Object obj, Field field, Class<T> rootType) {
    var expression = new Expression(field.getName(),
      new PropertyDescriptor(field.getName(), obj.getClass()).getReadMethod().invoke(obj));
    return resolverSpec(field.getAnnotation(Spec.class), expression, rootType);
  }

  <T> Specification<T> resolverSpec(Spec spec, Expression expression, Class<T> rootType) {
    return resolvers.stream().filter(resolver -> resolver.supports(spec))
      .findFirst()
      .map(resolver -> resolver.buildSpecification(spec, expression, rootType))
      .orElseThrow(); // TODO: remove or-else-throw
  }
}
