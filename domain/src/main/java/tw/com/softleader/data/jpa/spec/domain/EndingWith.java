package tw.com.softleader.data.jpa.spec.domain;

import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;

/**
 * @author Matt Ho
 */
public class EndingWith<T> extends SimpleSpecification<T> {

  public EndingWith(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path, "%" + value);
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    return builder.like(path(root), Objects.toString(value));
  }
}
