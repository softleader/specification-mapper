package tw.com.softleader.data.jpa.spec.bind;

import static java.util.Optional.of;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import tw.com.softleader.data.jpa.spec.bind.annotation.Spec;
import tw.com.softleader.data.jpa.spec.util.FieldUtil;

/**
 * @author Matt Ho
 */
@Slf4j
public class SimpleSpecificationResolver implements SpecificationResolver {

  @Override
  public Class<? extends Annotation> getSupportedSpecificationDefinition() {
    return Spec.class;
  }

  @Override
  @SneakyThrows
  public Specification<Object> buildSpecification(@NonNull Object obj, @NonNull Field field) {
    var def = field.getAnnotation(Spec.class);
    log.debug("Building specification of [{}.{}] for @Spec(path=\"{}\", spec={}.class)",
        obj.getClass().getSimpleName(),
        field.getName(),
        def.path(),
        def.spec().getSimpleName());
    var value = FieldUtil.getValue(obj, field);
    if (value == null) {
      return null;
    }
    return def.spec().getConstructor(String.class, Object.class)
        .newInstance(of(def.path()).filter(StringUtils::hasText).orElseGet(field::getName), value);
  }
}
