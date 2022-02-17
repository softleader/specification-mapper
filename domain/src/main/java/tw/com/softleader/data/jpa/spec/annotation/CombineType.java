package tw.com.softleader.data.jpa.spec.annotation;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import tw.com.softleader.data.jpa.spec.domain.AndSpecification;
import tw.com.softleader.data.jpa.spec.domain.OrSpecification;

/**
 * @author Matt Ho
 */
@RequiredArgsConstructor
public enum CombineType {
  /**
   * Respecting the default combine algorithm
   */
  RESPECT(Function.identity()),
  /**
   * Force using {@link Specification#and(Specification)} to combine Specs
   */
  AND(AndSpecification::new),
  /**
   * Force using {@link Specification#or(Specification)} to combine Specs
   */
  OR(OrSpecification::new),
  ;

  private final Function<Specification<Object>, Specification<Object>> combiner;

  public Specification<Object> apply(@Nullable Specification<Object> spec) {
    return combiner.apply(spec);
  }
}
