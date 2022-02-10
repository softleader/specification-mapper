package tw.com.softleader.data.jpa.spec.domain;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;
import lombok.ToString;

/**
 * 實作 equal, e.g. {@code where name = "MyName"}
 *
 * @author Matt Ho
 */
@ToString
public class NotEqual<T> extends SimpleSpecification<T> {

  public NotEqual(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path, value);
  }

  @Override
  public Predicate toPredicate(Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder builder) {
    return builder.notEqual(path(root), value);
  }
}