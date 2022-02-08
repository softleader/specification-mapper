package tw.com.softleader.data.jpa.spec;

import java.lang.reflect.Field;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import tw.com.softleader.data.jpa.spec.domain.Context;

/**
 * @author Matt Ho
 */
public interface SpecificationResolver {

  boolean supports(@NonNull Object obj, @NonNull Field field);

  @Nullable
  Specification<Object> buildSpecification(@NonNull Context context, @NonNull Object obj, @NonNull Field field);
}
