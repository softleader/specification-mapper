package tw.com.softleader.data.jpa.spec.bind;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Field;
import java.util.Optional;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import tw.com.softleader.data.jpa.spec.bind.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Equal;
import tw.com.softleader.data.jpa.spec.domain.Metadata;
import tw.com.softleader.data.jpa.spec.domain.Spec.Factory;
import tw.com.softleader.data.jpa.spec.util.FieldUtil;

/**
 * 這隻程式負責綁定 spec object 跟 field
 *
 * @author Matt Ho
 */
@Slf4j
@UtilityClass
public class ConjunctionSpecifications {

  public ConjunctionSpecification<?> of(
      @NonNull Compose compose,
      @NonNull Specification<?> delegate) {
    switch (compose) {
      case AND:
        return and(delegate);
      case OR:
        return or(delegate);
      default:
        throw new UnsupportedOperationException("Unsupported spec.compose: " + compose);
    }
  }

  public ConjunctionSpecification<?> and(@NonNull Specification<?> specification) {
    if (specification instanceof ConjunctionSpecification) {
      return new AndSpecification<>(
          ((ConjunctionSpecification<?>) specification).getUnderlyingSpecification());
    }
    return new AndSpecification<>(specification);
  }

  public ConjunctionSpecification<?> or(@NonNull Specification specification) {
    if (specification instanceof ConjunctionSpecification) {
      return new OrSpecification<>(
          ((ConjunctionSpecification<?>) specification).getUnderlyingSpecification());
    }
    return new OrSpecification<>(specification);
  }

  public ConjunctionSpecification<?> of(@NonNull Object obj, @NonNull Field field) {
    requireNonNull(obj, "obj is marked @NonNull but is null");
    requireNonNull(field, "field is marked @NonNull but is null");

    var spec = field.getAnnotation(Spec.class);
    if (spec == null) {
      log.debug("No @Spec set on [{}.{}], falling back to default: and-specification",
          obj.getClass().getSimpleName(),
          field.getName());
      var metadata = Metadata.builder()
          .path(field.getName())
          .value(FieldUtil.getValue(obj, field))
          .build();
      return and(Factory.INSTANCE.create(Equal.class).build(metadata));
    }
    log.debug("Building specification of [{}.{}] for @Spec(compose={}, path=\"{}\", spec={}.class)",
        obj.getClass().getSimpleName(), field.getName(),
        spec.compose(), spec.path(), spec.spec().getSimpleName());
    var metadata = Metadata.builder()
        .path(Optional.of(spec.path()).filter(StringUtils::hasText).orElseGet(field::getName))
        .value(FieldUtil.getValue(obj, field))
        .build();
    return of(spec.compose(), Factory.INSTANCE.create(spec.spec()).build(metadata));
  }
}
