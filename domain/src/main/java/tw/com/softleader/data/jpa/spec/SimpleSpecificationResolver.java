package tw.com.softleader.data.jpa.spec;

import static java.util.Optional.of;
import static org.springframework.data.jpa.domain.Specification.not;
import static org.springframework.util.ReflectionUtils.accessibleConstructor;

import java.util.stream.StreamSupport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.SimpleSpecification;

/**
 * @author Matt Ho
 */
@Slf4j
class SimpleSpecificationResolver implements SpecificationResolver {

  @Override
  public boolean supports(@NonNull Databind databind) {
    return databind.getField().isAnnotationPresent(Spec.class);
  }

  @Override
  public Specification<Object> buildSpecification(@NonNull Context context,
      @NonNull Databind databind) {
    var def = databind.getField().getAnnotation(Spec.class);
    log.debug("Building specification of [{}.{}] for @Spec(path=\"{}\", spec={}.class)",
        databind.getTarget().getClass().getSimpleName(),
        databind.getField().getName(),
        def.path(),
        def.value().getSimpleName());
    return databind.getFieldValue()
        .filter(this::valuePresent)
        .map(value -> {
          var path = of(def.path())
              .filter(StringUtils::hasText)
              .orElseGet(databind.getField()::getName);
          var spec = newSimpleSpecification(context, def.value(), path, value);
          if (def.not()) {
            spec = not(spec);
          }
          return def.combineType().apply(spec);
        }).orElseGet(() -> {
          log.debug("Value of [{}.{}] is null or empty, skipping it",
              databind.getTarget().getClass().getSimpleName(),
              databind.getField().getName());
          return null;
        });
  }

  boolean valuePresent(Object value) {
    if (value instanceof Iterable) {
      return StreamSupport.stream(((Iterable<?>) value).spliterator(), false).count() > 0;
    }
    return true;
  }

  @SneakyThrows
  private Specification<Object> newSimpleSpecification(
      @NonNull Context context,
      @NonNull Class<? extends SimpleSpecification> domainClass,
      @NonNull String path,
      @NonNull Object value) {
    return accessibleConstructor(domainClass, Context.class, String.class, Object.class)
        .newInstance(context, path, value);
  }
}
