package tw.com.softleader.data.jpa.spec;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import tw.com.softleader.data.jpa.spec.domain.Context;

/**
 * @author Matt Ho
 */
public interface SpecificationResolver {

  boolean supports(@NonNull Databind databind);

  @Nullable
  Specification<Object> buildSpecification(@NonNull Context context, @NonNull Databind databind);
}
