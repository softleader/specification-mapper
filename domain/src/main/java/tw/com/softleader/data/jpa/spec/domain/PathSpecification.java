package tw.com.softleader.data.jpa.spec.domain;

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PROTECTED;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
@RequiredArgsConstructor(access = PROTECTED)
public abstract class PathSpecification<T> implements Specification<T> {

  @NonNull
  protected final Context context;
  @NonNull
  protected final String path;

  protected <F> Path<F> path(Root<T> root) {
    var split = path.split("\\.");
    if (split.length == 1) {
      return root.get(split[0]);
    }
    Path<?> expr = null;
    for (String field : split) {
      if (expr == null) {
        expr = ofNullable(context.getJoin(field, root))
            .map(join -> (Path<T>) join)
            .orElseGet(() -> root.get(field));
        continue;
      }
      expr = expr.get(field);
    }
    return (Path<F>) expr;
  }

}
