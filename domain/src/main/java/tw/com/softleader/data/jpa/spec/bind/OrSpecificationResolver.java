package tw.com.softleader.data.jpa.spec.bind;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import tw.com.softleader.data.jpa.spec.bind.annotation.Or;
import tw.com.softleader.data.jpa.spec.domain.Disjunction;
import tw.com.softleader.data.jpa.spec.util.FieldUtil;

@RequiredArgsConstructor
public class OrSpecificationResolver implements SpecificationResolver {

  final SimpleSpecificationResolver specResolver;

  @Override
  public Class<? extends Annotation> getSupportedSpecificationDefinition() {
    return Or.class;
  }

  @Nullable
  @Override
  public Specification<Object> buildSpecification(@NonNull Object obj, @NonNull Field field) {
    var nested = FieldUtil.getValue(obj, field);
    if (nested == null) {
      return null;
    }
    var innerSpecs = FieldUtil.fieldStream(nested)
        .filter(f -> f.isAnnotationPresent(specResolver.getSupportedSpecificationDefinition()))
        .map(f -> specResolver.buildSpecification(nested, f))
        .filter(Objects::nonNull)
        .collect(toUnmodifiableList());
    return innerSpecs.isEmpty() ? null : new Disjunction<>(innerSpecs);
  }

}
