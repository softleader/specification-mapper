package tw.com.softleader.data.jpa.spec;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.bind.AndSpecificationResolver;
import tw.com.softleader.data.jpa.spec.bind.OrSpecificationResolver;
import tw.com.softleader.data.jpa.spec.bind.SimpleSpecificationResolver;
import tw.com.softleader.data.jpa.spec.bind.SpecificationResolver;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.util.FieldUtil;

/**
 * @author Matt Ho
 */
@Slf4j
public class SpecMapper implements SpecCodec {

  final Collection<SpecificationResolver> resolvers;

  public SpecMapper() {
    var simpleSpecificationResolver = new SimpleSpecificationResolver();
    this.resolvers = Stream.of(
        simpleSpecificationResolver,
        new OrSpecificationResolver(simpleSpecificationResolver),
        new AndSpecificationResolver(simpleSpecificationResolver)).collect(toCollection(LinkedList::new));
  }

  @Override
  public <T> Specification<T> toSpec(Object obj, Class<T> rootType) {
    if (obj == null) {
      return null;
    }
    return (Specification<T>) resolveSpec(obj);
  }

  Specification<Object> resolveSpec(@NonNull Object obj) {
    var specs = FieldUtil.fieldStream(obj)
        .map(field -> resolveSpec(obj, field))
        .filter(Objects::nonNull)
        .collect(toList());
    if (specs.size() == 0) {
      return null;
    }
    if (specs.size() == 1) {
      return specs.iterator().next();
    }
    return new Conjunction<>(specs);
  }

  Specification<Object> resolveSpec(@NonNull Object obj, @NonNull Field field) {
    return resolvers.stream()
        .filter(resolver -> field.isAnnotationPresent(resolver.getSupportedSpecificationDefinition()))
        .findFirst()
        .map(resolver -> resolver.buildSpecification(obj, field))
        .orElse(null);
  }
}
