package tw.com.softleader.data.jpa.spec.domain;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;

/**
 * @author Matt Ho
 */
public class Like<T> extends PathSpecification<T> {

  protected String value;

  public Like(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path);
    this.value = "%s" + value + "%s";
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    return builder.like(path(root), value);
  }
}
