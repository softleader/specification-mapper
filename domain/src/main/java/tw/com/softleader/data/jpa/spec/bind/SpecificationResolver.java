package tw.com.softleader.data.jpa.spec.bind;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * @author Matt Ho
 */
public interface SpecificationResolver {

  @NonNull
  Class<? extends Annotation> getSupportedSpecificationDefinition();

  @Nullable
  Specification<Object> buildSpecification(@NonNull Object obj, @NonNull Field field);
}
