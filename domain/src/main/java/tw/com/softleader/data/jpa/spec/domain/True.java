package tw.com.softleader.data.jpa.spec.domain;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;

/**
 * {@code ... where x.active = true} or {@code ... where x.active = false}
 *
 * @author Matt Ho
 */
public class True<T> extends BooleanSpecification<T> {

  public True(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path, value);
  }

  @Override
  public Predicate toPredicate(Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder builder) {
    if (getValue()) {
      return builder.isTrue(path(root));
    }
    return builder.isFalse(path(root));
  }
}
