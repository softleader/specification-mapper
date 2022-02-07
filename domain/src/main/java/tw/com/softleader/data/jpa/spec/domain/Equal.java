package tw.com.softleader.data.jpa.spec.domain;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 實作 equal, e.g. {@code where name = "MyName"}
 *
 * @author Matt Ho
 */
@ToString
@RequiredArgsConstructor
public class Equal<T> extends PathSpecification<T> {

  @Getter
  @NonNull
  final String path;
  @NonNull
  final Object value;

  @Override
  public Predicate toPredicate(Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder builder) {
    return builder.equal(path(root), value);
  }
}
