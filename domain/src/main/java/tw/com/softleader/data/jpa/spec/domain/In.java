package tw.com.softleader.data.jpa.spec.domain;

import static java.util.Optional.of;

import java.util.Collection;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;

public class In<T> extends SimpleSpecification<T> {

  private Collection value;

  public In(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path,
        of(value).map(val -> {
          if (val instanceof Collection) {
            return ((Collection) val);
          }
          if (val.getClass().isArray()) {
            return List.of((Object[]) val);
          }
          return null;
        }).orElseThrow(() -> new TypeMismatchException(value, Collection.class, Object[].class)));
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    return path(root).in(value);
  }
}
