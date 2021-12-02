package tw.com.softleader.data.jpa.spec.domain;

import lombok.SneakyThrows;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.domain.annotation.Spec;

class SpecResolver implements SpecificationResolver<Spec> {

  @SneakyThrows
  @Override
  public <R> Specification<R> buildSpecification(
    Spec spec, Expression expression, Class<R> rootType) {
    return spec.spec()
      .getDeclaredConstructor(String.class, Object.class) // TODO 這邊要想個更好的方式 new instance
      .newInstance(
        expression.competePath(spec.path()), expression.getValue()
      );
  }
}
