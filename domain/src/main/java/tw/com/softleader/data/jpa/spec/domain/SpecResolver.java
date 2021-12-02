package tw.com.softleader.data.jpa.spec.domain;

import lombok.SneakyThrows;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.domain.annotation.Spec;

class SpecResolver implements SpecificationResolver<Spec> {

  @SneakyThrows
  @Override
  public <R> Specification<R> buildSpecification(
    Spec spec, Expression expression, Class<R> rootType) {
    return spec.spec().getDeclaredConstructor(String.class, Object.class).newInstance(
      expression.competePath(spec.path()), expression.getValue()
    );
  }
}
