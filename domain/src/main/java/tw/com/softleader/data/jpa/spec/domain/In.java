package tw.com.softleader.data.jpa.spec.domain;

import static java.util.stream.StreamSupport.stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;

public class In<T> extends SimpleSpecification<T> {

  public In(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path, value);
    if (!(value instanceof Iterable)) {
      throw new TypeMismatchException(value, Iterable.class);
    }
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    return path(root).in(
        stream(((Iterable) value).spliterator(), false).toArray(Object[]::new));
  }
}
