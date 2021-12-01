package tw.com.softleader.data.jpa.spec.domain;

import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
public class SpecMapper implements SpecCodec {

  @Override
  public <T> Specification<T> toSpec(@NonNull T rootObject) {
    return null;
  }

  @Override
  public <T> Specification<T> toSpec(Object obj, Class<T> rootType) {
    return null;
  }
}
