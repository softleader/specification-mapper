package tw.com.softleader.data.jpa.spec.domain;

import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

/**
 * @author Matt Ho
 */
public interface SpecCodec {

  <T> Specification<T> toSpec(@NonNull T rootObject);

  <T> Specification<T> toSpec(@NonNull Object object, @Nullable Class<T> rootType);

}
