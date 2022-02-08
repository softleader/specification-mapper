package tw.com.softleader.data.jpa.spec.util;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import javax.persistence.Column;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

@Slf4j
@UtilityClass
public class FieldUtil {

  public Optional<String> getJpaColumnName(@Nullable Field field) {
    return ofNullable(field)
        .map(f -> f.getAnnotation(Column.class))
        .map(Column::name)
        .filter(StringUtils::hasText);
  }

  public Stream<Field> fieldStream(@Nullable Object obj) {
    if (obj == null) {
      return Stream.empty();
    }
    return Arrays.stream(obj.getClass().getDeclaredFields());
  }

  @SneakyThrows
  public Object getValue(@Nullable Object obj, @NonNull Field field) {
    if (obj == null) {
      return null;
    }
    return getReadMethod(obj, field).invoke(obj);
  }

  public Method getReadMethod(@NonNull Object obj, @NonNull Field field) {
    try {
      return new PropertyDescriptor(field.getName(), obj.getClass()).getReadMethod();
    } catch (IntrospectionException e) {
      throw new ReadMethodAccessException(
          format(
              "Error getting read method for [%s.%s], make sure that you have defined getter for the field.",
              obj.getClass().getSimpleName(),
              field.getName()),
          e);
    }
  }
}
