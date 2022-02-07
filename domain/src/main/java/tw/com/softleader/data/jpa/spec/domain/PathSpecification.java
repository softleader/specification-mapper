package tw.com.softleader.data.jpa.spec.domain;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public abstract class PathSpecification<T> implements Specification<T> {

  protected abstract String getPath();

  protected Path<?> path(Root<?> root) {
    Path<?> expr = null;
    for (String field : getPath().split("\\.")) {
      if (expr == null) {
        expr = root.get(field);
      } else {
        expr = expr.get(field);
      }
    }
    return expr;
  }
}
