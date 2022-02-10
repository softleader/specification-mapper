package tw.com.softleader.data.jpa.spec;

import static java.util.Optional.of;
import static java.util.function.Predicate.not;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import tw.com.softleader.data.jpa.spec.annotation.And;
import tw.com.softleader.data.jpa.spec.annotation.Or;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.Disjunction;

/**
 * @author Matt Ho
 */
@Slf4j
@RequiredArgsConstructor
class CompositionSpecificationResolver implements SpecificationResolver {

  final SpecCodec codec;

  @Override
  public boolean supports(@NonNull Databind databind) {
    return databind.getField().isAnnotationPresent(Or.class)
        || databind.getField().isAnnotationPresent(And.class);
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
          var spec = of(codec.collectSpecs(context, nested))
              .filter(not(Collection::isEmpty))
              .map(newDomain(databind.getField())::apply)
              .orElse(null);
          log.debug(" <- Composed specification from [{}.{}]: {}",
              databind.getTarget().getClass().getSimpleName(),
              databind.getField().getName(),
              spec);
          return spec;
        })
        .orElse(null);
  }

  <T> Function<Collection<Specification<T>>, Specification<T>> newDomain(Field field) {
    return field.isAnnotationPresent(Or.class) ? Disjunction::new : Conjunction::new;
  }
}
