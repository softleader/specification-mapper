package tw.com.softleader.data.jpa.spec.domain;

import static java.util.Optional.ofNullable;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

/**
 * To constraint the constructor, the implementations must provide accessible constructor.
 *
 * @author Matt Ho
 */
public abstract class SimpleSpecification<T> implements Specification<T> {

  protected final Context context;
  protected final String path;
  protected final Object value;

  public SimpleSpecification(@NonNull Context context, @NonNull String path,
      @NonNull Object value) {
    this.context = context;
    this.path = path;
    this.value = value;
  }

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
