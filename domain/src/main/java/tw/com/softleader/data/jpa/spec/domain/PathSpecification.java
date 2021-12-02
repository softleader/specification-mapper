package tw.com.softleader.data.jpa.spec.domain;

import static java.util.Arrays.stream;
import static lombok.AccessLevel.PROTECTED;

import java.util.Objects;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

/**
 * 實作的 subclass 必須提供 Spring, Object 且 public 的 Constructor
 *
 * @author Matt Ho
 */
@RequiredArgsConstructor
@EqualsAndHashCode(of = "path")
public abstract class PathSpecification<T> implements Specification<T> {

  @NonNull
  private final String path;
  @Getter(PROTECTED)
  @NonNull
  private final Object value;

  @SuppressWarnings("unchecked")
  protected <F> Path<F> path(Root<T> root) {
    return (Path<F>) stream(path.split("\\."))
      .map(root::get)
      .filter(Objects::nonNull)
      .findFirst()
      .orElse(null);
  }
}
