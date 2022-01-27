package tw.com.softleader.data.jpa.spec;

import static java.util.Arrays.stream;

import java.util.Objects;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.bind.Databind;
import tw.com.softleader.data.jpa.spec.bind.SpecificationResolver;

/**
 * @author Matt Ho
 */
@Builder
public class SpecMapper implements SpecCodec {

  @Override
  public <T> Specification<T> toSpec(Object obj, Class<T> rootType) {

    return (Specification<T>) stream(obj.getClass().getDeclaredFields())
        .map(field -> Databind.of(obj, field))
        .filter(Objects::nonNull)
        .sorted()
        .map(Databind::getResolver)
        .reduce(SpecificationResolver::squash)
        .map(SpecificationResolver::get)
        .orElse(null);
  }

  SpecificationResolver bindDeclaredFields(Object obj) {
    if (obj == null) {
      return null;
    }
    return stream(obj.getClass().getDeclaredFields())
        .map(field -> Databind.of(obj, field))
        .sorted()
        .map(Databind::getResolver)
        .reduce(SpecificationResolver::squash)
        .orElse(null);
  }

}
