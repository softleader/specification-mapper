package tw.com.softleader.data.jpa.spec.domain;

import java.util.Collection;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
@RequiredArgsConstructor
abstract class CompoundSpecification<T> implements Specification<T> {

  @NonNull
  protected final transient Collection<Specification<T>> specs;

  @Override
  public Predicate toPredicate(Root root,
      CriteriaQuery query,
      CriteriaBuilder builder) {
    return specs.stream().reduce(this::combine)
        .map(spec -> spec.toPredicate(root, query, builder))
        .orElse(null);
  }

  /**
   * @param result 到目前 Combine 的結果
   * @param element 下一個元素
   */
  protected abstract Specification<T> combine(
      Specification<T> result,
      Specification<T> element);
}
