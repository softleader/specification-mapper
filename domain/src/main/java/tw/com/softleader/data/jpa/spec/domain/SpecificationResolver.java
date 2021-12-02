package tw.com.softleader.data.jpa.spec.domain;

import static org.springframework.core.ResolvableType.forClass;

import java.lang.annotation.Annotation;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationResolver<T extends Annotation> {

  default <A extends Annotation> boolean supports(@NonNull A annotation) {
    return forClass(getClass()).as(SpecificationResolver.class).getGeneric(0)
      .resolve().isInstance(annotation);
  }

  <R> Specification<R> buildSpecification(T annotation, Expression expression, Class<R> rootType);
}
