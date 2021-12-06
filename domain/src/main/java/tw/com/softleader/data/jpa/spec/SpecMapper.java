package tw.com.softleader.data.jpa.spec;

import static java.util.Arrays.stream;

import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.bind.Databind;

/**
 * @author Matt Ho
 */
@Builder
public class SpecMapper implements SpecCodec {

  @Override
  public <T> Specification<T> toSpec(Object obj, Class<T> rootType) {
    return (Specification<T>) stream(obj.getClass().getDeclaredFields())
        .map(field -> new Databind(obj, field))
        .sorted()
        .reduce(Databind::reduce)
        .map(Databind::getSpec)
        .orElse(null);
  }
}
