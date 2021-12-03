package tw.com.softleader.data.jpa.spec.bind;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.bind.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Metadata;

class SpecSpecificationResolver implements SpecificationResolver<Spec> {

  @SneakyThrows
  @Override
  public Specification<?> buildSpecification(@NonNull Metadata metadata) {
    var spec = metadata.getSpecType().getDeclaredConstructor().newInstance();
    return spec.build(metadata);
  }
}
