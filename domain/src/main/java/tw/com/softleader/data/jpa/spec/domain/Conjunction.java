package tw.com.softleader.data.jpa.spec.domain;

import java.util.Collection;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
@ToString
public class Conjunction<T> extends CompoundSpecification<T> {

  public Conjunction(@NonNull Collection<Specification<T>> specs) {
    super(specs);
  }

  @Override
  protected Specification<T> combine(Specification<T> result,
      Specification<T> element) {
    if (element instanceof OrSpecification) {
      return result.or(element);
    }
    return result.and(element);
  }

}
