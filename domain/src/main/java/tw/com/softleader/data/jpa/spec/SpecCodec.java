package tw.com.softleader.data.jpa.spec;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.Disjunction;

/**
 * @author Matt Ho
 */
public interface SpecCodec {

  /**
   * @return null if non any {@code Specification} was mapped
   */
  @SuppressWarnings("unchecked")
  @Nullable
  default Specification<Object> toSpec(@Nullable Object rootObject) {
    return of(collectSpecs(new SpecContext(), rootObject))
        .filter(not(Collection::isEmpty))
        .map(specs -> rootObject.getClass().isAnnotationPresent(
            tw.com.softleader.data.jpa.spec.annotation.Or.class) ? new Disjunction<>(specs)
                : new Conjunction<>(specs))
        .orElse(null);
  }

  /**
   * @return null if non any {@code Specification} was mapped
   */
  @Nullable
  @SuppressWarnings("unchecked")
  default <T> Specification<T> toSpec(@Nullable Object rootObject, @Nullable Class<T> rootType) {
    return (Specification<T>) toSpec(rootObject);
  }

  /**
   * @return empty if non any {@code Specification} was mapped
   */
  @NonNull
  default Optional<Specification<Object>> trySpec(@Nullable Object rootObject) {
    return ofNullable(toSpec(rootObject));
  }

  /**
   * @return empty if non any {@code Specification} was mapped
   */
  @NonNull
  default <T> Optional<Specification<T>> trySpec(@Nullable T rootObject,
      @Nullable Class<T> rootType) {
    return ofNullable(toSpec(rootObject, rootType));
  }

  /**
   * Collect and mapping every field in object to {@code Specification}
   */
  @NonNull
  Collection<Specification<Object>> collectSpecs(@NonNull Context context,
      @Nullable Object rootObject);
}
