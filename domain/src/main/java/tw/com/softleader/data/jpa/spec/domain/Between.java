package tw.com.softleader.data.jpa.spec.domain;

import static java.util.stream.StreamSupport.stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;

/**
 * {@code ... where x.age between ? and ?}
 *
 * @author Matt Ho
 */
public class Between<T> extends SimpleSpecification<T> {

  public Between(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path, value);
    if (!(value instanceof Iterable)) {
      throw new TypeMismatchException(value, Iterable.class);
    }
    if (stream(((Iterable<?>) value).spliterator(), false).count() != 2) {
      throw new IllegalArgumentException("@Between expected exact 2 elements, but was " + value);
    }
  }

  @Override
  public Predicate toPredicate(Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder builder) {
    var args = stream(((Iterable<?>) value).spliterator(), false)
        .map(arg -> (Comparable<?>) arg)
        .toArray(Comparable[]::new);
    return builder.between(getPath(root), args[0], args[1]);
  }
}
