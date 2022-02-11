package tw.com.softleader.data.jpa.spec.domain;

import static java.util.Optional.ofNullable;

import java.util.Collection;
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
public class Disjunction<T> implements Specification<T> {

  private final Collection<Specification<T>> innerSpecs;

  public Disjunction(@NonNull Collection<Specification<T>> innerSpecs) {
    this.innerSpecs = innerSpecs;
  }

  @Override
  public Predicate toPredicate(Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder builder) {
    Specification<T> combinedSpecs = null;
    for (Specification<T> spec : innerSpecs) {
      if (combinedSpecs == null) {
        combinedSpecs = Specification.where(spec);
        continue;
      }
      combinedSpecs = combinedSpecs.or(spec);
    }
    return ofNullable(combinedSpecs)
        .map(spec -> spec.toPredicate(root, query, builder))
        .orElse(null);
  }
}
