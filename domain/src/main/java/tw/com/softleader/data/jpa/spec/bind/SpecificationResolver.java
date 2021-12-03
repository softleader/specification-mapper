package tw.com.softleader.data.jpa.spec.bind;

import static org.springframework.core.ResolvableType.forClass;

import java.lang.annotation.Annotation;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.domain.Metadata;

/**
 * Specification 解析器, 負責 {@code Specification} 的建構
 *
 * @author Matt Ho
 */
public interface SpecificationResolver<T extends Annotation> {

  /**
   * @return true 如果此解析器可以處理所傳入的 annotation
   */
  default boolean supports(@NonNull T annotation) {
    return forClass(getClass()).as(SpecificationResolver.class).getGeneric(0)
      .resolve().isInstance(annotation);
  }

  Specification<?> buildSpecification(@NonNull Metadata metadata);
}
