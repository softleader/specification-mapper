package tw.com.softleader.data.jpa.spec.domain;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;
import lombok.ToString;

/**
 * {@code ... where x.firstname = ?}
 *
 * @author Matt Ho
 */
@ToString
public class Equals<T> extends SimpleSpecification<T> {

  public Equals(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path, value);
  }

  @Override
  public Predicate toPredicate(Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder builder) {
    return builder.equal(getPath(root), value);
  }
}
