package tw.com.softleader.data.jpa.spec.util;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.persistence.Column;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Matt Ho
 */
@Slf4j
@UtilityClass
public class FieldUtil {

  private final Map<Class<?>, Field[]> declaredFieldsCache = new ConcurrentReferenceHashMap<>(256);
  private final Field[] EMPTY_FIELD_ARRAY = new Field[0];

  public Optional<String> getJpaColumnName(@Nullable Field field) {
    return ofNullable(field)
        .map(f -> f.getAnnotation(Column.class))
        .map(Column::name)
        .filter(StringUtils::hasText);
  }

  public Stream<Field> fieldStream(@Nullable Object target) {
    if (target == null) {
      return Stream.empty();
    }
    return fieldStream(target.getClass());
  }

  public Stream<Field> fieldStream(@NonNull Class<?> clazz) {
    Field[] result = declaredFieldsCache.get(clazz);
    if (result == null) {
      try {
        result = clazz.getDeclaredFields();
        declaredFieldsCache.put(clazz, (result.length == 0 ? EMPTY_FIELD_ARRAY : result));
      } catch (Throwable ex) {
        throw new IllegalStateException("Failed to introspect Class [" + clazz.getName() +
            "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
      }
    }
    return stream(result);
  }

  @SneakyThrows
  public Object getValue(@Nullable Object target, @NonNull Field field) {
    if (target == null) {
      return null;
    }
    ReflectionUtils.makeAccessible(field);
    return ReflectionUtils.getField(field, target);
  }
}
