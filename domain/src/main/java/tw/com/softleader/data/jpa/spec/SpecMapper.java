package tw.com.softleader.data.jpa.spec;

import static java.util.Arrays.stream;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.SneakyThrows;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.bind.SpecificationResolver;
import tw.com.softleader.data.jpa.spec.bind.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Metadata;

/**
 * @author Matt Ho
 */
@Builder
public class SpecMapper implements SpecCodec {

  @Singular
  List<SpecificationResolver> resolvers;

  @Override
  public <T> Specification<T> toSpec(Object obj, Class<T> rootType) {
    return (Specification<T>) stream(obj.getClass().getDeclaredFields())
      .map(field -> toSpec(obj, field))
      .reduce((left, right) -> left.and(right)) // TODO and 應該要可以透過 annotation 指定
      .get(); // TODO: remove get and cast
  }

  @SneakyThrows
  private Specification<?> toSpec(Object obj, Field field) {
    var spec = field.getAnnotation(Spec.class);
    var metadata =
      Metadata.builder()
        .path(spec.path())
        .value(new PropertyDescriptor(field.getName(), obj.getClass()).getReadMethod().invoke(obj))
        .order(spec.order())
        .specType(spec.spec())
        .build();
    return resolverSpec(spec, metadata);
  }

  Specification<?> resolverSpec(Annotation spec, Metadata metadata) {
    return resolvers.stream().filter(resolver -> resolver.supports(spec))
      .findFirst()
      .map(resolver -> resolver.buildSpecification(metadata))
      .orElseThrow(); // TODO: remove or-else-throw
  }
}
