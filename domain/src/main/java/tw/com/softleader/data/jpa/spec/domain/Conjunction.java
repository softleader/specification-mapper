package tw.com.softleader.data.jpa.spec.domain;

import java.util.Collection;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
@ToString
public class Conjunction<T> implements Specification<T> {

  final Collection<Specification<T>> innerSpecs;

  public Conjunction(@NonNull Collection<Specification<T>> innerSpecs) {
    this.innerSpecs = innerSpecs;

    if (innerSpecs.isEmpty()) {
      throw new IllegalArgumentException("innerSpecs must not be empty");
    }
  }

  @Override
  public Predicate toPredicate(Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder builder) {
    return builder.and(
        innerSpecs.stream()
            .map(spec -> spec.toPredicate(root, query, builder))
            .filter(Objects::nonNull)
            .toArray(Predicate[]::new));
  }
}
