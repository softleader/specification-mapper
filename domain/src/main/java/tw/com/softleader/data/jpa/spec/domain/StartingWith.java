package tw.com.softleader.data.jpa.spec.domain;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;

/**
 * @author Matt Ho
 */
public class StartingWith<T> extends Like<T> {

  public StartingWith(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path, value + "%");
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    return builder.like(path(root), (String) value);
  }
}
