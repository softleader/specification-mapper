package tw.com.softleader.data.jpa.spec.util;

import static org.springframework.util.ReflectionUtils.doWithLocalFields;
import static org.springframework.util.ReflectionUtils.getField;
import static org.springframework.util.ReflectionUtils.makeAccessible;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * @author Matt Ho
 */
@Slf4j
@UtilityClass
public class FieldUtil {

  /**
   * Get all locally declared fields of stream in the given object's class. (will not going up the
   * class hierarchy)
   */
  public List<Field> getLocalFields(@Nullable Object target) {
    if (target == null) {
      return List.of();
    }
    return getLocalFields(target.getClass());
  }

  /**
   * Get all locally declared fields of stream in the given class. (will not going up the class
   * hierarchy)
   */
  public List<Field> getLocalFields(@NonNull Class<?> clazz) {
    var lookup = new ArrayList<Field>();
    doWithLocalFields(clazz, lookup::add);
    return lookup;
  }

  @SneakyThrows
  public Object getValue(@Nullable Object target, @NonNull Field field) {
    if (target == null) {
      return null;
    }
    makeAccessible(field);
    return getField(field, target);
  }
}
