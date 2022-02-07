package tw.com.softleader.data.jpa.spec;

import static java.util.Optional.ofNullable;

import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

/**
 * @author Matt Ho
 */
public interface SpecCodec {

  /**
   * @return null if can not mapper to {@code Specification}
   */
  @SuppressWarnings("unchecked")
  @Nullable
  default <T> Specification<T> toSpec(@NonNull T rootObject) {
    return toSpec(rootObject, (Class<T>) rootObject.getClass());
  }

  /**
   * @return null if can not mapper to {@code Specification}
   */
  @Nullable
  <T> Specification<T> toSpec(@NonNull Object object, @Nullable Class<T> rootType);

  @org.springframework.lang.NonNull
  default <T> Optional<Specification<T>> trySpec(@NonNull T rootObject) {
    return ofNullable(toSpec(rootObject));
  }

  @org.springframework.lang.NonNull
  default <T> Optional<Specification<T>> trySpec(@NonNull T rootObject,
      @Nullable Class<T> rootType) {
    return ofNullable(toSpec(rootObject, rootType));
  }
}
