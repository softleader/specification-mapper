package tw.com.softleader.data.jpa.spec.bind;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.domain.Metadata;
import tw.com.softleader.data.jpa.spec.domain.Spec;

@RequiredArgsConstructor
class OrSpecificationResolver implements SpecificationResolver {

  @NonNull
  final Metadata metadata;
  @NonNull
  final Class<? extends Spec> specType;

  @Override
  public Specification get() {
    return SPEC_FACTORY.create(specType).build(metadata);
  }

  @Override
  public SpecificationResolver squash(SpecificationResolver other) {
    return () -> this.get().or(other.get());
  }
}
