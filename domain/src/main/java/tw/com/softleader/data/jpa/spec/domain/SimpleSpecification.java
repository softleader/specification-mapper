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

  protected final transient Context context;
  protected final String path;
  protected final transient Object value;

  protected SimpleSpecification(@NonNull Context context, @NonNull String path,
      @NonNull Object value) {
    this.context = context;
    this.path = path;
    this.value = value;
  }

  protected <F> Path<F> getPath(Root<T> root) {
    var split = path.split("\\.");
    if (split.length == 1) {
      return root.get(split[0]);
    }
    Path<?> expr = null;
    for (String field : split) {
      if (expr == null) {
        expr = ofNullable(context.getJoin(field, root))
            .map(joined -> (Path<T>) joined)
            .orElseGet(() -> root.get(field));
        continue;
      }
      expr = expr.get(field);
    }
    return (Path<F>) expr;
  }
}
