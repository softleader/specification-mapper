package tw.com.softleader.data.jpa.spec.domain;

import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;

/**
 * {@code ... where x.firstname like %?%}
 *
 * @author Matt Ho
 */
public class Like<T> extends SimpleSpecification<T> {

  public Like(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path, "%" + value + "%");
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    return builder.like(getPath(root), Objects.toString(value));
  }
}
