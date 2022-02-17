package tw.com.softleader.data.jpa.spec.domain;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Delegate;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
@AllArgsConstructor
public class AndSpecification<T> implements Specification<T> {

  @NonNull
  @Delegate
  Specification<T> delegate;
}
