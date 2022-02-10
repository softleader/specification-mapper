package tw.com.softleader.data.jpa.spec;

import java.util.Collection;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import tw.com.softleader.data.jpa.spec.annotation.NestedSpec;
import tw.com.softleader.data.jpa.spec.annotation.Or;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.Disjunction;

/**
 * @author Matt Ho
 */
@Slf4j
@RequiredArgsConstructor
class NestedSpecificationResolver implements SpecificationResolver {

  final SpecCodec codec;

  @Override
  public boolean supports(@NonNull Databind databind) {
    return databind.getField().isAnnotationPresent(NestedSpec.class);
  }

  @Override
  public Specification<Object> buildSpecification(@NonNull Context context,
      @NonNull Databind databind) {
    return databind.getFieldValue()
        .map(nested -> {
          log.debug(" -> Looping fields through nested object [{}.{}] ({})",
              databind.getTarget().getClass().getSimpleName(),
              databind.getField().getName(),
              databind.getField().getType());
          var spec = codec.toSpec(context, nested);
          log.debug(" <- Composed specification from [{}.{}]: {}",
              databind.getTarget().getClass().getSimpleName(),
              databind.getField().getName(),
              spec);
          return spec;
        })
        .orElse(null);
  }

  <T> Function<Collection<Specification<T>>, Specification<T>> newDomain(Databind databind) {
    log.debug("determine composite logic for field [{}], is annotation @Or on field class [{}]? {}",
        databind.getField().getName(),
        databind.getTarget().getClass().getSimpleName(),
        databind.getTarget().getClass().isAnnotationPresent(Or.class));
    return databind.getTarget().getClass().isAnnotationPresent(Or.class)
        ? Disjunction::new
        : Conjunction::new;
  }
}
