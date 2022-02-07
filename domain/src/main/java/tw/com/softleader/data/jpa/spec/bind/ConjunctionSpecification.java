package tw.com.softleader.data.jpa.spec.bind;

import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
public interface ConjunctionSpecification<T> extends Specification<T> {

  ConjunctionSpecification<T> composed(ConjunctionSpecification<T> other);

  Specification<T> getUnderlyingSpecification();
}
