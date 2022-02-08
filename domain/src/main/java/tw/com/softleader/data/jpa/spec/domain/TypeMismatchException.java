package tw.com.softleader.data.jpa.spec.domain;

import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * @author Matt Ho
 */
@Getter
public class TypeMismatchException extends RuntimeException {

  @Nullable
  private final transient Object value;

  @Nullable
  private final Class<?> requiredType;

  public TypeMismatchException(@Nullable Object value, @Nullable Class<?> requiredType,
      Class<?>... requiredTypes) {
    this(null, value, requiredType, requiredTypes);
  }

  public TypeMismatchException(@Nullable Throwable cause, @Nullable Object value,
      @Nullable Class<?> requiredType,
      Class<?>... requiredTypes) {
    super("Failed to convert value of type '" + ClassUtils.getDescriptiveType(value) + "'" +
        (requiredType != null ? " to required type '"
            + Stream.concat(Stream.of(requiredType), Stream.of(requiredTypes))
                .map(ClassUtils::getQualifiedName).collect(
                    joining(" or "))
            + "'" : ""),
        cause);
    this.value = value;
    this.requiredType = requiredType;
  }
}
