package tw.com.softleader.data.jpa.spec;

import static java.util.Optional.of;
import static org.springframework.util.ReflectionUtils.accessibleConstructor;

import java.lang.reflect.Field;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.SimpleSpecification;
import tw.com.softleader.data.jpa.spec.util.FieldUtil;

/**
 * @author Matt Ho
 */
@Slf4j
class SimpleSpecificationResolver implements SpecificationResolver {

  @Override
  public boolean supports(@NonNull Object obj, @NonNull Field field) {
    return field.isAnnotationPresent(Spec.class);
  }

  @Override
  public Specification<Object> buildSpecification(@NonNull Context context, @NonNull Object obj,
      @NonNull Field field) {
    var def = field.getAnnotation(Spec.class);
    log.debug("Building specification of [{}.{}] for @Spec(path=\"{}\", spec={}.class)",
        obj.getClass().getSimpleName(),
        field.getName(),
        def.path(),
        def.value().getSimpleName());
    var value = FieldUtil.getValue(obj, field);
    if (value == null) {
      log.debug("Value of [{}.{}] is null, skipping it",
          obj.getClass().getSimpleName(),
          field.getName());
      return null;
    }
    var path = of(def.path())
        .filter(StringUtils::hasText)
        .orElseGet(field::getName);
    return newSimpleSpecification(context, def.value(), path, value);
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
