package tw.com.softleader.data.jpa.spec.domain;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;

/**
 * @author Matt Ho
 */
public class GreaterThan<T> extends PathSpecification<T> {

  private Comparable value;

  public GreaterThan(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path);
    if (!(value instanceof Comparable)) {
      throw new IllegalArgumentException("value must implements java.lang.Comparable");
    }
    this.value = (Comparable) value;
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    return builder.greaterThan(path(root), value);
  }
}
