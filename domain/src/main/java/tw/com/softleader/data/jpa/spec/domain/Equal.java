package tw.com.softleader.data.jpa.spec.domain;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;

/**
 * 實作 where equals, e.g. {@code where name = "MyName"}
 *
 * @author Matt Ho
 */
public class Equal<T> extends PathSpecification<T> {

  public Equal(@NonNull String path, @NonNull Object value) {
    super(path, value);
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    return cb.equal(path(root), getValue());
  }
}
