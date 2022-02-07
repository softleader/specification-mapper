package tw.com.softleader.data.jpa.spec.bind;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import tw.com.softleader.data.jpa.spec.bind.annotation.And;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.util.FieldUtil;

/**
 * @author Matt Ho
 */
@Slf4j
@RequiredArgsConstructor
public class AndSpecificationResolver implements SpecificationResolver {

  final SimpleSpecificationResolver specResolver;

  @Override
  public Class<? extends Annotation> getSupportedSpecificationDefinition() {
    return And.class;
  }

  @Override
  public Specification<Object> buildSpecification(@NonNull Object obj, @NonNull Field field) {
    var nested = FieldUtil.getValue(obj, field);
    if (nested == null) {
      return null;
    }
    log.debug(" -> Looping fields through nested object [{}.{}] ({})",
        obj.getClass().getSimpleName(),
        field.getName(),
        FieldUtil.getReadMethod(obj, field).getReturnType().getName());
    var innerSpecs = FieldUtil.fieldStream(nested)
        .filter(f -> f.isAnnotationPresent(specResolver.getSupportedSpecificationDefinition()))
        .map(f -> specResolver.buildSpecification(nested, f))
        .filter(Objects::nonNull)
        .collect(toUnmodifiableList());
    var spec = innerSpecs.isEmpty() ? null : new Conjunction<>(innerSpecs);
    log.debug(" <- Composed specification from [{}.{}]: {}",
        obj.getClass().getSimpleName(),
        field.getName(),
        spec);
    return spec;
  }

}
