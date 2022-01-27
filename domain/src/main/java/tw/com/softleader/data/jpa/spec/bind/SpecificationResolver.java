package tw.com.softleader.data.jpa.spec.bind;

import java.util.function.Supplier;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.domain.Spec.Factory;

public interface SpecificationResolver extends Supplier<Specification> {

  Factory SPEC_FACTORY = new Factory();

  default SpecificationResolver squash(SpecificationResolver other) {
    return () -> this.get().and(other.get());
  }
}
