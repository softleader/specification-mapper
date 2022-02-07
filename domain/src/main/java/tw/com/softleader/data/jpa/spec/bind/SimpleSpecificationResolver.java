package tw.com.softleader.data.jpa.spec.bind;

import static java.util.Optional.of;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import lombok.SneakyThrows;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import tw.com.softleader.data.jpa.spec.bind.annotation.Spec;
import tw.com.softleader.data.jpa.spec.util.FieldUtil;

public class SimpleSpecificationResolver implements SpecificationResolver {

  @Override
  public Class<? extends Annotation> getSupportedSpecificationDefinition() {
    return Spec.class;
  }

  @Nullable
  @Override
  @SneakyThrows
  public Specification<Object> buildSpecification(@NonNull Object obj, @NonNull Field field) {
    var def = field.getAnnotation(Spec.class);
    var value = FieldUtil.getValue(obj, field);
    if (value == null) {
      return null;
    }
    return def.spec().getConstructor(String.class, Object.class)
        .newInstance(of(def.path()).filter(StringUtils::hasText).orElseGet(field::getName), value);
  }
}
