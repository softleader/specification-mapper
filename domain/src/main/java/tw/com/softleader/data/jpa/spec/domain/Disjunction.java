package tw.com.softleader.data.jpa.spec.domain;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.stream.Stream;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.data.jpa.domain.Specification;

@ToString
@RequiredArgsConstructor
public class Disjunction<T> implements Specification<T> {

  final Collection<Specification<T>> innerSpecs;

  public Disjunction(@NonNull Specification<T> innerSpec, Specification<T>... innerSpecs) {
    this(Stream.concat(Stream.of(innerSpec), Stream.of(innerSpecs)).collect(toList()));
  }

  @Override
  public Predicate toPredicate(Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder builder) {
    if (innerSpecs.isEmpty()) {
      throw new IllegalStateException("innerSpecs must not be empty");
    }
    Specification<T> combinedSpecs = null;
    for (Specification<T> spec : innerSpecs) {
      if (combinedSpecs == null) {
        combinedSpecs = Specification.where(spec);
        continue;
      }
      combinedSpecs = combinedSpecs.or(spec);
    }
    return combinedSpecs.toPredicate(root, query, builder);
  }
}
