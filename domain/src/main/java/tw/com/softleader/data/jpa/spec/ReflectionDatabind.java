package tw.com.softleader.data.jpa.spec;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;
import static org.springframework.util.ReflectionUtils.doWithLocalFields;
import static org.springframework.util.ReflectionUtils.makeAccessible;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * Databind implementation using Spring's {@code ReflectionUtils}
 *
 * @see ReflectionUtils
 */
@Value
class ReflectionDatabind implements Databind {

  @NonNull
  Object target;
  @NonNull
  Field field;

  static List<Databind> of(@Nullable Object target) {
    if (target == null) {
      return List.of();
    }
    var lookup = new ArrayList<Databind>();
    doWithLocalFields(target.getClass(),
        field -> lookup.add(new ReflectionDatabind(target, field)));
    return unmodifiableList(lookup);
  }

  @Override
  public Optional<Object> getFieldValue() {
    makeAccessible(field);
    return ofNullable(ReflectionUtils.getField(field, target))
        .flatMap(value -> {
          if (value instanceof Optional) {
            return (Optional) value;
          }
          return Optional.of(value);
        });
  }
}
