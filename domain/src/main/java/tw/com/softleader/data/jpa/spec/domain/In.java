package tw.com.softleader.data.jpa.spec.domain;

import java.util.Collection;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;

public class In<T> extends PathSpecification<T> {

  private Collection value;

  public In(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path);

    if (value instanceof Collection) {
      this.value = ((Collection) value);
    } else if (value.getClass().isArray()) {
      this.value = List.of((Object[]) value);
    } else {
      throw new IllegalArgumentException("value must be Collection or Array");
    }

  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    return path(root).in(value);
  }
}
