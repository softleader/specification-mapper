package tw.com.softleader.data.jpa.spec.domain;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;

/**
 * @author Matt Ho
 */
public class LessThanOrEqualTo<T> extends SimpleSpecification<T> {

  public LessThanOrEqualTo(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path, value);
    if (!(value instanceof Comparable)) {
      throw new TypeMismatchException(value, Comparable.class);
    }
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    return builder.lessThanOrEqualTo(path(root), (Comparable) value);
  }
}
