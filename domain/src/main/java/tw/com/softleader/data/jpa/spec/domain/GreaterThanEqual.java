package tw.com.softleader.data.jpa.spec.domain;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;

/**
 * {@code ... where x.age >= ?}
 *
 * @author Matt Ho
 */
public class GreaterThanEqual<T> extends ComparableSpecification<T> {

  public GreaterThanEqual(@NonNull Context context, @NonNull String path,
      @NonNull Object value) {
    super(context, path, value);
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    return builder.greaterThanOrEqualTo(path(root), getValue());
  }
}
