package tw.com.softleader.data.jpa.spec.domain;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;

/**
 * {@code ... where x.age is null}
 *
 * @author Matt Ho
 */
public class Null<T> extends BooleanSpecification<T> {

  public Null(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path, value);
  }

  @Override
  public Predicate toPredicate(Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder builder) {
    if (getValue()) {
      return builder.isNull(path(root));
    }
    return builder.isNotNull(path(root));
  }
}
