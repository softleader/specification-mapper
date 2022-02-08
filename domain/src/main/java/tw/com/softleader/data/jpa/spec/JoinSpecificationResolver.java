package tw.com.softleader.data.jpa.spec;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import tw.com.softleader.data.jpa.spec.annotation.Join;
import tw.com.softleader.data.jpa.spec.annotation.Joins;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Context;

/**
 * @author Matt Ho
 */
@Slf4j
class JoinSpecificationResolver implements SpecificationResolver {

  @Override
  public boolean supports(@NonNull Object obj, @NonNull Field field) {
    return field.isAnnotationPresent(Join.class) || field.isAnnotationPresent(Joins.class);
  }

  @Override
  public Specification<Object> buildSpecification(@NonNull Context context, @NonNull Object obj,
      @NonNull Field field) {
    var specs = Stream.concat(
        joinDef(context, field), joinsDef(context, field)).filter(Objects::nonNull)
        .collect(toList());
    if (specs.size() == 1) {
      return specs.get(0);
    }
    return new Conjunction<>(specs);
  }

  private Stream<Specification<Object>> joinsDef(Context context, Field field) {
    if (!field.isAnnotationPresent(Joins.class)) {
      return Stream.empty();
    }
    return stream(field.getAnnotation(Joins.class).values())
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