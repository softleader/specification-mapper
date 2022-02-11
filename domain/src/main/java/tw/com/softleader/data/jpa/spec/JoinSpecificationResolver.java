package tw.com.softleader.data.jpa.spec;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import tw.com.softleader.data.jpa.spec.annotation.Join;
import tw.com.softleader.data.jpa.spec.annotation.Join.Joins;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Context;

/**
 * @author Matt Ho
 */
@Slf4j
class JoinSpecificationResolver implements SpecificationResolver {

  @Override
  public boolean supports(@NonNull Databind databind) {
    return (databind.getField().isAnnotationPresent(Join.class)
        || databind.getField().isAnnotationPresent(Joins.class));
  }

  @Override
  public Specification<Object> buildSpecification(@NonNull Context context,
      @NonNull Databind databind) {
    return databind.getFieldValue()
        .filter(this::valuePresent)
        .map(value -> {
          var specs = Stream.concat(
              joinDef(context, databind.getField()),
              joinsDef(context, databind.getField()))
              .filter(Objects::nonNull)
              .collect(toList());
          if (specs.size() == 1) {
            return specs.get(0);
          }
          return new Conjunction<>(specs);
        }).orElse(null);
  }

  boolean valuePresent(Object value) {
    if (value instanceof Iterable) {
      return StreamSupport.stream(((Iterable<?>) value).spliterator(), false).count() > 0;
    }
    return true;
  }

  private Stream<Specification<Object>> joinsDef(Context context, Field field) {
    if (!field.isAnnotationPresent(Joins.class)) {
      return Stream.empty();
    }
    return stream(field.getAnnotation(Joins.class).value())
        .map(def -> newJoin(context, def));
  }

  private Stream<Specification<Object>> joinDef(Context context, Field field) {
    if (!field.isAnnotationPresent(Join.class)) {
      return Stream.empty();
    }
    return Stream.of(
        newJoin(context, field.getAnnotation(Join.class)));
  }

  Specification<Object> newJoin(@NonNull Context context, @NonNull Join def) {
    return new tw.com.softleader.data.jpa.spec.domain.Join<>(
        context,
        def.path(),
        def.alias(),
        def.joinType(),
        def.distinct());
  }
}
