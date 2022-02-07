package tw.com.softleader.data.jpa.spec.bind;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
@ToString
@RequiredArgsConstructor
class OrSpecification<T> implements ConjunctionSpecification<T> {

  @NonNull
  @Delegate
  private final Specification<T> delegate;

  @Override
  public ConjunctionSpecification<T> composed(ConjunctionSpecification<T> before) {
    return new OrSpecification<>(before.or(this));
  }

  @Override
  public Specification<T> getUnderlyingSpecification() {
    return delegate;
  }
}
