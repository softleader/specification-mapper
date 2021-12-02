package tw.com.softleader.data.jpa.spec.domain;

import static org.springframework.core.ResolvableType.forClass;

import java.lang.annotation.Annotation;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification 解析器, 負責 {@code Specification} 的建構
 *
 * @author Matt Ho
 */
public interface SpecificationResolver<T extends Annotation> {

  /**
   * @return true 如果此解析器可以處理所傳入的 annotation
   */
  default <A extends Annotation> boolean supports(@NonNull A annotation) {
    return forClass(getClass()).as(SpecificationResolver.class).getGeneric(0)
      .resolve().isInstance(annotation);
  }

  <R> Specification<R> buildSpecification(T annotation, Expression expression, Class<R> rootType);
}
